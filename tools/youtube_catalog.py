"""Bangun katalog YouTube Xflix dari playlist channel @ardikusnadi71.

Jalankan ulang bila channel menambah video:
    python tools/youtube_catalog.py
Hasil: app/src/main/resources/youtube_catalog.json
"""
import json
import re
import sys
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

CHANNEL = "https://www.youtube.com/@ardikusnadi71/playlists"
OUT = Path(__file__).resolve().parent.parent / "app/src/main/resources/youtube_catalog.json"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0 Safari/537.36"

COPAQUE = "Les' Copaque Production"
KIDS = ["Upin Ipin", "animasi", "anak", "keluarga"]

# split: "code" = musim dari kode SxxEyy, "playlist" = satu playlist satu musim, "none" = satu musim
TITLES = [
    dict(id="yt-upin-ipin", name="Upin & Ipin", playlists=["Upin & Ipin"], split="code",
         category="Kartun", year=2007, creator=COPAQUE, genres=["Comedy"], moods=KIDS, poster="last",
         synopsis="Si kembar Upin dan Ipin tumbuh bersama Kak Ros, Opah, dan kawan-kawan di Kampung Durian Runtuh. "
                  "Musim 1 sampai 19, dari Ramadan pertama sampai petualangan terbaru."),
    dict(id="yt-upin-ipin-iqra", name="Upin & Ipin Iqra",
         playlists=["Upin Ipin Iqra Musim 1", "Upin Ipin Iqra Musim 2", "Upin Ipin Iqra Musim 3"], split="playlist",
         category="Kartun", creator=COPAQUE, genres=["Documentary"], moods=KIDS + ["Islami", "edukasi"],
         synopsis="Belajar surah pendek, salat, dan kisah para nabi bersama Upin dan Ipin."),
    dict(id="yt-upin-ipin-puasa-raya", name="Upin & Ipin: Puasa & Raya", playlists=["Upin Ipin Puasa & Raya"],
         split="none", category="Kartun", creator=COPAQUE, genres=["Comedy"], moods=KIDS + ["Ramadan", "Lebaran"],
         synopsis="Kumpulan episode Ramadan dan Hari Raya Upin & Ipin dari musim ke musim."),
    dict(id="yt-upin-ipin-kembara", name="Upin & Ipin: Kembara ke Pulau Harta Karun",
         playlists=["Upin Ipin Kembara Ke Pulau Harta Karun"], split="none", category="Kartun", creator=COPAQUE,
         genres=["Action"], moods=KIDS + ["petualangan"],
         synopsis="Petualangan Upin, Ipin, dan kawan-kawan mencari harta karun di pulau misterius."),
    dict(id="yt-upin-ipin-metrobot", name="Upin & Ipin: MetroBot & Neo Santara",
         playlists=["Upin Ipin MetroBot + Neo Santara"], split="none", category="Kartun", creator=COPAQUE,
         genres=["SciFi"], moods=KIDS + ["robot"],
         synopsis="Episode pilihan saat Upin dan Ipin berubah menjadi pahlawan MetroBot."),
    dict(id="yt-upin-ipin-pin-pin-pom", name="Upin & Ipin: Pin Pin Pom", playlists=["Upin Ipin Pin Pin Pom"],
         split="none", category="Kartun", creator=COPAQUE, genres=["SciFi"], moods=KIDS + ["sihir"],
         synopsis="Kisah ajaib Pin Pin Pom dari beberapa musim Upin & Ipin."),
    dict(id="yt-upin-ipin-sebuah", name="Upin & Ipin: Aku Sebuah", playlists=["Upin Ipin Sebuah"],
         split="none", category="Kartun", creator=COPAQUE, genres=["Comedy"], moods=KIDS,
         synopsis="Cerita dari sudut pandang benda-benda di sekitar Upin dan Ipin."),
    dict(id="pzd-pada-zaman-dahulu", name="Pada Zaman Dahulu (PZD)", playlists=["Pada Zaman Dahulu"], split="code",
         category="Kartun", year=2011, creator=COPAQUE, genres=["Comedy"], moods=["PZD", "animasi", "anak", "cerita rakyat"],
         synopsis="Ara dan Aris mendengar cerita rakyat dari Aki, dari Sang Kancil sampai fabel binatang lainnya."),
]


def get(url, data=None):
    req = urllib.request.Request(url, data=data, headers={
        "User-Agent": UA, "Accept-Language": "id-ID,id;q=0.9", "Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=40) as r:
        return r.read().decode("utf-8")


def walk(o, found, tokens):
    if isinstance(o, dict):
        if "lockupViewModel" in o:
            found.append(o["lockupViewModel"])
        command = o.get("continuationCommand")
        if isinstance(command, dict) and command.get("token"):
            tokens.append(command["token"])
        for v in o.values():
            walk(v, found, tokens)
    elif isinstance(o, list):
        for v in o:
            walk(v, found, tokens)


def browse_all(url):
    """Semua lockup di halaman, termasuk lanjutan."""
    html = get(url)
    i = html.find("var ytInitialData = ")
    data = json.JSONDecoder().raw_decode(html[i + len("var ytInitialData = "):])[0]
    key = re.search(r'"INNERTUBE_API_KEY":"([^"]+)"', html).group(1)
    ver = re.search(r'"INNERTUBE_CLIENT_VERSION":"([^"]+)"', html).group(1)
    found, tokens, seen = [], [], set()
    walk(data, found, tokens)
    while tokens:
        token = tokens.pop(0)
        if token in seen:
            continue
        seen.add(token)
        body = json.dumps({"context": {"client": {"clientName": "WEB", "clientVersion": ver, "hl": "id"}},
                           "continuation": token}).encode()
        walk(json.loads(get(f"https://www.youtube.com/youtubei/v1/browse?key={key}&prettyPrint=false", body)), found, tokens)
    return found


def title_of(lockup):
    return lockup.get("metadata", {}).get("lockupMetadataViewModel", {}).get("title", {}).get("content", "").strip()


def embeddable(video_id):
    url = f"https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v={video_id}&format=json"
    try:
        with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": UA}), timeout=30):
            return True
    except urllib.error.HTTPError:
        return False


CODE = re.compile(r"\bS(\d{1,2})E(\d{1,2})(?:\s*(?:&|dan)\s*(?:S\d{1,2})?E?(\d{1,2}))?|\bS(\d{2})(\d{2})\b", re.I)


def parse_code(raw):
    """(musim, episode, episode kedua) dari SxxEyy atau Sxxyy."""
    m = CODE.search(raw)
    if not m:
        return None
    if m.group(4):
        return int(m.group(4)), int(m.group(5)), None
    return int(m.group(1)), int(m.group(2)), int(m.group(3)) if m.group(3) else None


def after_dash(text):
    return text.split(" - ", 1)[1].strip() if " - " in text else text


def episode_name(raw, split, position):
    code = parse_code(raw)
    if split == "code" and code:
        number = code[1] if code[2] is None else f"{code[1]} & {code[2]}"
        return f"Episode {number} - {after_dash(raw)}"
    if split == "playlist":
        return f"Episode {position} - {after_dash(raw)}"
    if code:
        return f"{after_dash(raw)} (S{code[0]}E{code[1]:02d})"
    return after_dash(raw)


def build():
    playlists = {title_of(p): p["contentId"] for p in browse_all(CHANNEL)
                 if p.get("contentType") == "LOCKUP_CONTENT_TYPE_PLAYLIST"}
    needed = {name for spec in TITLES for name in spec["playlists"]}
    videos = {}
    for name, pid in playlists.items():
        if name not in needed:
            continue
        videos[name] = [(v["contentId"], title_of(v)) for v in browse_all(f"https://www.youtube.com/playlist?list={pid}")
                        if v.get("contentType") == "LOCKUP_CONTENT_TYPE_VIDEO"]
        print(f"{len(videos[name]):4}  {name}", flush=True)

    used = {v for spec in TITLES for name in spec["playlists"] for v, _ in videos.get(name, [])}
    with ThreadPoolExecutor(12) as pool:
        ok = dict(zip(sorted(used), pool.map(embeddable, sorted(used))))

    titles = []
    for spec in TITLES:
        seasons = {}
        for index, name in enumerate(spec["playlists"], 1):
            if name not in videos:
                print(f"PERINGATAN: playlist '{name}' tidak ada", file=sys.stderr)
                continue
            for vid, raw in videos[name]:
                if not ok.get(vid):
                    continue
                code = parse_code(raw)
                number = code[0] if spec["split"] == "code" and code else index if spec["split"] == "playlist" else 1
                season = seasons.setdefault(number, [])
                if all(e["id"] != vid for e in season):
                    season.append({"id": vid, "name": episode_name(raw, spec["split"], len(season) + 1)})
        if not seasons:
            continue
        ordered = [{"number": n, "episodes": seasons[n]} for n in sorted(seasons)]
        # Pemutar memetakan musim per urutan
        if sorted(seasons) != list(range(1, len(seasons) + 1)):
            print(f"PERINGATAN: musim {spec['name']} tidak berurutan: {sorted(seasons)}", file=sys.stderr)
        poster = ordered[-1]["episodes"][0]["id"] if spec.get("poster") == "last" else ordered[0]["episodes"][0]["id"]
        entry = {k: spec[k] for k in ("id", "name", "category", "creator", "genres", "moods", "synopsis")}
        if "year" in spec:
            entry["year"] = spec["year"]
        entry.update(poster=poster, seasons=ordered)
        titles.append(entry)
        print(f"  -> {spec['name']}: {len(ordered)} musim, {sum(len(s['episodes']) for s in ordered)} video")

    skipped = sorted(v for v, good in ok.items() if not good)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps({"source": CHANNEL, "titles": titles}, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"Tersimpan {OUT} ({len(titles)} judul, {len(skipped)} video tidak bisa di-embed)")


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")
    build()
