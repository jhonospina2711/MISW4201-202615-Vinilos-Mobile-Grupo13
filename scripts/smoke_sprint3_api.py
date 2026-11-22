#!/usr/bin/env python3
"""Verify the real BackVynils contract for HU05, HU06 and HU08.

Read-only by default. --create-track writes one uniquely named track to the
selected local album; use it only with a disposable development database.
"""

import argparse
import json
import sys
from typing import Optional
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen
from uuid import uuid4


def request_json(base_url: str, path: str, payload: Optional[dict] = None):
    body = json.dumps(payload).encode() if payload is not None else None
    request = Request(
        base_url.rstrip("/") + "/" + path.lstrip("/"),
        data=body,
        headers={"Content-Type": "application/json"} if body else {},
        method="POST" if body else "GET",
    )
    with urlopen(request, timeout=15) as response:
        assert response.status == 200, f"{path}: HTTP {response.status}"
        return json.load(response)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:3000/")
    parser.add_argument("--create-track", action="store_true")
    args = parser.parse_args()

    collectors = request_json(args.base_url, "collectors")
    assert isinstance(collectors, list) and collectors, "GET /collectors: lista vacía"
    collector = collectors[0]
    assert isinstance(collector.get("id"), int) and collector.get("name")
    assert isinstance(collector.get("favoritePerformers"), list)
    assert isinstance(collector.get("collectorAlbums"), list)
    print(f"PASS HU05 GET /collectors ({len(collectors)} registros)")

    collector_id = collector["id"]
    detail = request_json(args.base_url, f"collectors/{collector_id}")
    relationships = request_json(args.base_url, f"collectors/{collector_id}/albums")
    assert detail.get("id") == collector_id
    assert isinstance(detail.get("comments"), list)
    assert isinstance(relationships, list)
    assert all(isinstance(item.get("album"), dict) for item in relationships)
    print(
        f"PASS HU06 GET /collectors/{collector_id} y /albums "
        f"({len(relationships)} álbumes)"
    )

    albums = request_json(args.base_url, "albums")
    assert isinstance(albums, list) and albums
    album_id = albums[0]["id"]
    tracks = request_json(args.base_url, f"albums/{album_id}/tracks")
    assert isinstance(tracks, list)
    print(f"PASS HU08 GET /albums/{album_id}/tracks ({len(tracks)} tracks)")

    if args.create_track:
        name = f"Vinilos QA {uuid4().hex[:8]}"
        created = request_json(
            args.base_url,
            f"albums/{album_id}/tracks",
            {"name": name, "duration": "03:21"},
        )
        assert isinstance(created.get("id"), int)
        assert created.get("name") == name and created.get("duration") == "03:21"
        updated = request_json(args.base_url, f"albums/{album_id}/tracks")
        assert any(track.get("id") == created["id"] for track in updated)
        print(
            f"PASS HU08 POST /albums/{album_id}/tracks y lectura posterior "
            f"(track {created['id']})"
        )


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, HTTPError, URLError, ValueError, KeyError, TypeError) as error:
        print(f"FAIL {error}", file=sys.stderr)
        sys.exit(1)
