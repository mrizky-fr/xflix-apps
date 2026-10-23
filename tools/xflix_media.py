"""Unduh episode YouTube, dibundel ke APK."""
import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

DEFAULT_OUT = Path.home() / "Videos" / "Xflix"


def ytdlp(*args: str, capture: bool = False) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, "-m", "yt_dlp", "--no-playlist", "--no-warnings", *args],
        check=True,
        capture_output=capture,
        text=True,
        encoding="utf-8",
    )


def episode_title(raw: str, series: str) -> str:
    # Buang akhiran nama serial
    return re.sub(rf"\s*[-–|]\s*{re.escape(series)}\s*$", "", raw, flags=re.IGNORECASE).strip()


def main() -> None:
    p = argparse.ArgumentParser(description="Unduh episode YouTube untuk Xflix")
    p.add_argument("title_id", help="id judul di Catalog.kt, mis. sekolah_normal")
    p.add_argument("name", help="nama serial, dibuang dari judul video")
    p.add_argument("urls", nargs="+", help="link YouTube, urut sesuai episode")
    p.add_argument("--season", type=int, default=1)
    p.add_argument("--start", type=int, default=1, help="nomor episode pertama")
    p.add_argument("--height", type=int, default=720)
    p.add_argument("--out", type=Path, default=DEFAULT_OUT)
    args = p.parse_args()

    folder = args.out / args.title_id
    (folder / "thumbs").mkdir(parents=True, exist_ok=True)
    fmt = f"bv*[height<={args.height}][vcodec^=avc1]+ba[ext=m4a]/b[height<={args.height}][ext=mp4]"
    episodes = []

    for offset, url in enumerate(args.urls):
        number = args.start + offset
        stem = f"s{args.season:02d}e{number:02d}"
        info = json.loads(ytdlp("-j", url, capture=True).stdout)
        title = episode_title(info["title"], args.name)
        print(f"[{stem}] {title} ({round(info['duration'] / 60)} menit)")

        video = folder / f"{stem}.mp4"
        if video.exists():
            print("  sudah ada, dilewati")
        else:
            ytdlp("-f", fmt, "--merge-output-format", "mp4", "-o", str(video), url)
        ytdlp("--skip-download", "--write-thumbnail", "--convert-thumbnails", "jpg",
              "-o", str(folder / "thumbs" / stem), url)

        episodes.append({
            "season": args.season,
            "number": number,
            "title": title,
            "seconds": info["duration"],
            "videoId": info["id"],
            "file": video.name,
        })

    index = folder / "episodes.json"
    known = {(e["season"], e["number"]): e for e in json.loads(index.read_text("utf-8"))} if index.exists() else {}
    known.update({(e["season"], e["number"]): e for e in episodes})
    index.write_text(json.dumps(sorted(known.values(), key=lambda e: (e["season"], e["number"])),
                                ensure_ascii=False, indent=2), "utf-8")
    print(f"Selesai: {folder}")
    print("Build ulang APK agar video ikut masuk.")


if __name__ == "__main__":
    main()
