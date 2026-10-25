#!/usr/bin/env python3
"""Read-only contract smoke test for Sprint 1 against a running BackVynils API."""

import json
import sys
from urllib.parse import urljoin
from urllib.request import urlopen


def get_json(base_url: str, path: str):
    with urlopen(urljoin(base_url, path), timeout=10) as response:
        if response.status != 200:
            raise AssertionError(f"GET /{path}: HTTP {response.status}")
        return json.load(response)


def require_artist(artists: list, kind: str) -> int:
    assert isinstance(artists, list), f"GET /{kind} debe entregar una lista"
    assert artists, f"GET /{kind} debe tener al menos un registro en los datos de prueba"
    assert isinstance(artists[0].get("id"), int), f"GET /{kind}: falta id numérico"
    assert artists[0].get("name"), f"GET /{kind}: falta name"
    return artists[0]["id"]


def main() -> None:
    base_url = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:3000/"
    if not base_url.endswith("/"):
        base_url += "/"

    albums = get_json(base_url, "albums")
    assert isinstance(albums, list) and albums, "GET /albums debe tener datos de prueba"
    album_id = albums[0].get("id")
    assert isinstance(album_id, int) and albums[0].get("name"), "Álbum sin id o nombre"
    print(f"PASS HU01 GET /albums ({len(albums)} registros)")

    album = get_json(base_url, f"albums/{album_id}")
    assert album.get("id") == album_id and isinstance(album.get("tracks"), list)
    print(f"PASS HU02 GET /albums/{album_id} (tracks: {len(album['tracks'])})")

    musician_id = require_artist(get_json(base_url, "musicians"), "musicians")
    band_id = require_artist(get_json(base_url, "bands"), "bands")
    print("PASS HU03 GET /musicians y GET /bands")

    musician = get_json(base_url, f"musicians/{musician_id}")
    band = get_json(base_url, f"bands/{band_id}")
    assert musician.get("id") == musician_id and isinstance(musician.get("albums"), list)
    assert band.get("id") == band_id and isinstance(band.get("albums"), list)
    prizes = get_json(base_url, "performerprizes")
    assert isinstance(prizes, list), "GET /performerprizes debe entregar una lista"
    print("PASS HU04 detalle de músico, banda y catálogo de premios")


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, OSError, ValueError, KeyError) as error:
        print(f"FAIL {error}", file=sys.stderr)
        sys.exit(1)
