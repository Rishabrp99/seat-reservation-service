import concurrent.futures
import json
import sys
import time
import urllib.error
import urllib.request

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
SHOW_ID = int(sys.argv[2]) if len(sys.argv) > 2 else 12
SEAT = sys.argv[3] if len(sys.argv) > 3 else "A1"
REQUESTS = int(sys.argv[4]) if len(sys.argv) > 4 else 20000
WORKERS = int(sys.argv[5]) if len(sys.argv) > 5 else 500


def reserve(i):
    url = f"{BASE}/shows/{SHOW_ID}/reserve"
    body = json.dumps({"seats": [SEAT]}).encode("utf-8")

    req = urllib.request.Request(
        url,
        data=body,
        method="POST",
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer load-user-{i}-token",
            "Idempotency-Key": f"load-{i}",
        },
    )

    try:
        with urllib.request.urlopen(req, timeout=60) as response:
            return response.status
    except urllib.error.HTTPError as exc:
        return exc.code
    except Exception as exc:
        return "ERROR"


def main():
    print(f"BASE={BASE}")
    print(f"SHOW_ID={SHOW_ID}")
    print(f"SEAT={SEAT}")
    print(f"REQUESTS={REQUESTS}")
    print(f"WORKERS={WORKERS}")

    start = time.time()

    with concurrent.futures.ThreadPoolExecutor(max_workers=WORKERS) as pool:
        results = list(pool.map(reserve, range(REQUESTS)))

    elapsed = time.time() - start

    counts = {}
    for result in results:
        counts[result] = counts.get(result, 0) + 1

    print(f"elapsed_seconds={elapsed:.2f}")
    print(f"requests_per_second={REQUESTS / elapsed:.2f}")
    print("status_counts=" + json.dumps(counts))
    if counts.get(201, 0) != 1:
        raise SystemExit("FAIL: expected exactly one 201")

    if counts.get(500, 0) or counts.get("ERROR", 0):
        raise SystemExit("FAIL: found 500 or network errors")

    unexpected = sum(
        count for status, count in counts.items()
        if status not in (201, 409)
    )

    if unexpected:
        raise SystemExit("FAIL: unexpected HTTP status")

    if counts.get(409, 0) != REQUESTS - 1:
        raise SystemExit("FAIL: expected all remaining requests to be 409")

    print("PASS")


if __name__ == "__main__":
    main()
