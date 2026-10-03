#!/usr/bin/env bash
# Visite guidée filmée de Foteli sur la tablette virtuelle (aucune vraie donnée).
# Les boutons sont trouvés par leur texte à l'écran, comme le ferait une personne.
set -u
PKG=io.github.ploufty.foteli

find_text() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  adb pull /sdcard/ui.xml /tmp/ui.xml >/dev/null 2>&1
  python3 - "$1" <<'PY'
import re, sys, html
target = sys.argv[1]
xml = open('/tmp/ui.xml', encoding='utf-8').read()
for node in re.findall(r'<node [^>]*>', xml):
    text = html.unescape((re.search(r' text="([^"]*)"', node) or [None, ''])[1])
    desc = html.unescape((re.search(r' content-desc="([^"]*)"', node) or [None, ''])[1])
    if target in (text, desc):
        x1, y1, x2, y2 = map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node).groups())
        print((x1 + x2) // 2, (y1 + y2) // 2)
        break
PY
}

tap() {
  local pos
  pos=$(find_text "$1")
  if [ -z "$pos" ]; then echo "Introuvable à l'écran : $1"; return 1; fi
  adb shell input tap $pos
  sleep "${2:-1.5}"
}

pin() { for d in $(echo "$1" | grep -o .); do tap "$d" 0.4; done; }

type_text() { adb shell input text "$1"; sleep 1; }

hide_keyboard() { adb shell input keyevent 4; sleep 1; }

adb shell pm clear "$PKG"
adb shell screenrecord --time-limit 175 --bit-rate 6000000 /sdcard/visite.mp4 &
REC=$!
sleep 2

adb shell am start -W -n "$PKG/.MainActivity"
sleep 3
# 1. Premier lancement
tap "Créer ma classe"
pin 5731; sleep 1
pin 5731; sleep 4                      # empreinte du code : quelques secondes
sleep 2
tap "J’ai noté mon code"
tap "Nom de la classe" 1
type_text "MS-GS%sMme%sMartin"
hide_keyboard
tap "Continuer" 3
# 2. La classe : ajout groupé
tap "+ Ajouter plusieurs élèves"
tap "Un prénom par ligne" 1
for name in Adem Amir Lina Noah Jade Malo Sacha Ines; do
  adb shell input text "$name"; adb shell input keyevent 66
done
hide_keyboard
tap "Ajouter ces 8 élèves" 3
# 3. Changer la représentation d'un élève : Jade en prénom en grand
tap "Jade" 2
tap "Son prénom en grand" 2
tap "Enregistrer" 3
# 4. Mode élève
tap "Mode élève" 3
tap "Lina" 3
tap "‹" 2
# 5. Retour enseignant : appui long 3 s en haut à droite, puis le code
SIZE=$(adb shell wm size | grep -oE '[0-9]+x[0-9]+' | tail -1)
W=${SIZE%x*}
adb shell input swipe $((W - 90)) 140 $((W - 90)) 140 3600
sleep 2
pin 5731; sleep 4
tap "Réglages" 4
tap "Classe" 3

adb shell pkill -INT screenrecord
wait $REC 2>/dev/null
sleep 4
adb pull /sdcard/visite.mp4 visite-guidee.mp4
ls -la visite-guidee.mp4
