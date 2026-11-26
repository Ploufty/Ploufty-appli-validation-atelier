package io.github.ploufty.foteli.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.LruCache
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Taille maximale des photos enregistrées (côté le plus long) : assez pour un bilan, raisonnable pour la tablette. */
private const val MAX_PHOTO_SIDE = 2048

private val captureExecutor = Executors.newSingleThreadExecutor()

@Composable
fun rememberImageCapture(): ImageCapture = remember {
    ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .setResolutionStrategy(
                    ResolutionStrategy(Size(MAX_PHOTO_SIDE, MAX_PHOTO_SIDE * 3 / 4), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER),
                )
                .build(),
        )
        .build()
}

/**
 * Aperçu de la caméra, dans l'appli (jamais l'appli Appareil photo d'Android, sinon la photo finirait dans la galerie).
 * Si la caméra demandée n'existe pas sur la tablette, l'autre est utilisée.
 */
@Composable
fun CameraPreview(front: Boolean, imageCapture: ImageCapture, modifier: Modifier, onUnavailable: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    DisposableEffect(front, owner) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (disposed) return@addListener
            val p = runCatching { future.get() }.getOrNull() ?: return@addListener onUnavailable()
            provider = p
            val wanted = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            val other = if (front) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
            val selector = listOf(wanted, other).firstOrNull { runCatching { p.hasCamera(it) }.getOrDefault(false) }
                ?: return@addListener onUnavailable()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            try {
                p.unbindAll()
                p.bindToLifecycle(owner, selector, preview, imageCapture)
            } catch (e: Exception) {
                onUnavailable()
            }
        }, context.mainExecutor)
        onDispose {
            disposed = true
            provider?.unbindAll()
        }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}

/**
 * Prend la photo et l'écrit dans [target], redressée et réencodée : aucune métadonnée (modèle de tablette,
 * date, position) n'est conservée (docs/v0/technique.md § 3). [onResult] est appelé sur le fil principal.
 */
fun takePhoto(context: Context, imageCapture: ImageCapture, rotation: Int, target: File, onResult: (Boolean) -> Unit) {
    imageCapture.targetRotation = rotation
    imageCapture.takePicture(
        captureExecutor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val ok = runCatching { image.use { savePhoto(it.toBitmap(), it.imageInfo.rotationDegrees, target) } }.isSuccess
                context.mainExecutor.execute { onResult(ok) }
            }

            override fun onError(exception: ImageCaptureException) {
                context.mainExecutor.execute { onResult(false) }
            }
        },
    )
}

private fun savePhoto(source: Bitmap, rotationDegrees: Int, target: File) {
    val scale = minOf(1f, MAX_PHOTO_SIDE.toFloat() / maxOf(source.width, source.height))
    val matrix = Matrix().apply {
        postScale(scale, scale)
        postRotate(rotationDegrees.toFloat())
    }
    val bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    FileOutputStream(target).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
    if (bitmap !== source) bitmap.recycle()
    source.recycle()
}

/**
 * Robot ajouté par l'enseignant : l'image choisie est mise au carré (sans être déformée ni rognée),
 * réduite à 320 px et réenregistrée en PNG, transparence conservée, sans métadonnées.
 */
fun saveRobotImage(context: Context, uri: android.net.Uri, target: File): Boolean = runCatching {
    val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return false
    val side = 320
    val scale = side.toFloat() / maxOf(source.width, source.height)
    val w = (source.width * scale).toInt().coerceAtLeast(1)
    val h = (source.height * scale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(source, w, h, true)
    val square = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(square).drawBitmap(scaled, ((side - w) / 2).toFloat(), ((side - h) / 2).toFloat(), null)
    FileOutputStream(target).use { square.compress(Bitmap.CompressFormat.PNG, 100, it) }
    true
}.getOrDefault(false)

/**
 * Photo d'élève : recadrée au centre en carré, 512 px, JPEG sans métadonnées.
 * [open] fournit l'image d'origine (photo prise par Foteli ou image importée de la tablette).
 */
fun saveFaceImage(open: () -> java.io.InputStream?, target: File): Boolean = runCatching {
    val source = open()?.use { BitmapFactory.decodeStream(it) } ?: return false
    val side = minOf(source.width, source.height)
    val square = Bitmap.createBitmap(source, (source.width - side) / 2, (source.height - side) / 2, side, side)
    val small = Bitmap.createScaledBitmap(square, 512, 512, true)
    FileOutputStream(target).use { small.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    true
}.getOrDefault(false)

// ---------- Affichage des photos ----------

/** Vignettes déjà décodées : les noms de fichiers sont uniques et jamais réécrits. */
private val photoCache = object : LruCache<String, ImageBitmap>(48 * 1024 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
}

/** Photo du dossier privé, décodée hors du fil principal à la taille utile. */
@Composable
fun PhotoImage(file: File, modifier: Modifier, maxSide: Int = 1024, contentScale: ContentScale = ContentScale.Crop) {
    val key = "${file.path}@$maxSide"
    val bitmap by produceState(photoCache.get(key), key) {
        if (value == null) value = withContext(Dispatchers.IO) { decodePhoto(file, maxSide) }?.also { photoCache.put(key, it) }
    }
    val b = bitmap
    if (b != null) {
        Image(b, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier.background(Line))
    }
}

private fun decodePhoto(file: File, maxSide: Int): ImageBitmap? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
    return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
}
