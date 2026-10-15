"""Verify the deployed application over HTTP using only Python's standard library."""
import argparse
import json
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


def request(base, path, body=None):
    data = None if body is None else json.dumps(body).encode("utf-8")
    req = Request(base + path, data=data, headers={"Content-Type": "application/json"})
    with urlopen(req, timeout=30) as response:
        text = response.read().decode("utf-8")
        return response.status, text


def check(base):
    status, home = request(base, "/")
    assert status == 200 and "Daniel Esenwa" in home and "101199099" in home, "Wrong home page"
    status, text = request(base, "/api/addressbooks", {})
    assert status == 201, "Book was not created"
    book = json.loads(text)["id"]
    status, text = request(base, "/api/buddies", {
        "firstName": "Deployment", "lastName": "Check", "address": 123, "phone": 456,
        "email": "deployment@example.com"
    })
    assert status == 201, "Buddy was not created"
    buddy = json.loads(text)["id"]
    status, _ = request(base, f"/api/addressbooks/{book}/buddies", {"buddyId": buddy})
    assert status == 200, "Buddy was not attached"
    status, text = request(base, f"/api/addressbooks/{book}")
    assert status == 200 and json.loads(text)["buddies"][0]["email"] == "deployment@example.com", "Email missing from book JSON"
    status, text = request(base, f"/api/buddies/{buddy}")
    assert status == 200 and json.loads(text)["email"] == "deployment@example.com", "Email missing from buddy JSON"
    status, text = request(base, f"/addressbooks/{book}")
    assert status == 200 and "deployment@example.com" in text, "Email missing from HTML"
    print(f"PASS: home page, POST/GET workflow, email persistence, and HTML at {base}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("url")
    parser.add_argument("--attempts", type=int, default=30)
    parser.add_argument("--delay", type=float, default=10)
    args = parser.parse_args()
    if args.attempts < 1 or args.delay < 0:
        parser.error("attempts must be positive and delay must be nonnegative")
    base = args.url.rstrip("/")
    if not base.startswith(("http://", "https://")):
        parser.error("provide the application's full http:// or https:// URL")
    for attempt in range(1, args.attempts + 1):
        try:
            check(base)
            return
        except (HTTPError, URLError, TimeoutError, AssertionError, ValueError, KeyError, IndexError) as error:
            if attempt == args.attempts:
                raise SystemExit(f"FAIL after {attempt} attempts: {error}") from error
            print(f"Waiting for the app ({attempt}/{args.attempts}): {error}", flush=True)
            time.sleep(args.delay)


if __name__ == "__main__":
    main()
