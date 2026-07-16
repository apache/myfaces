#!/usr/bin/env bash
# Drives benchmark: warm up, reset phase stats, fire a measured run, print the per-phase table.
# GET readonly pages use curl URL-range globbing (fast, keep-alive). The form-inputs postback is a
# sequential ViewState round-trip, so its count is separate and small (per-phase timing is server-side
# and stable with a few hundred samples).
# Usage: bash drive.sh [GET_WARMUP] [GET_MEASURE] [LABEL] [PB_COUNT]
set -u
BASE="http://localhost:8080"
GW="${1:-100}"; GM="${2:-1500}"; LABEL="${3:-run}"; PB="${4:-200}"
COOKIES="$(mktemp)"
GETS=(table-readonly repeat-readonly foreach-readonly nested-readonly)

postback() {
  local view="$1"; local n="$2"; local resp names body vs i=0
  resp="$(curl -s -c "$COOKIES" -b "$COOKIES" "$BASE/$view.xhtml")"
  while [ "$i" -lt "$n" ]; do
    vs="$(printf '%s' "$resp" | grep -oE 'name="jakarta.faces.ViewState"[^>]*value="[^"]*"' | grep -oE 'value="[^"]*"' | head -1 | sed 's/^value="//; s/"$//')"
    [ -z "$vs" ] && { echo "  (no ViewState, stop)"; break; }
    names="$(printf '%s' "$resp" | grep -oE 'name="form:[^"]*"' | sed 's/^name="//; s/"$//' | sort -u)"
    body="form=form"
    while IFS= read -r nm; do [ -z "$nm" ] && continue; body="$body&$(printf '%s' "$nm" | sed 's/:/%3A/g')=v"; done <<< "$names"
    body="$body&form%3Asubmit=Submit&jakarta.faces.ViewState=$(printf '%s' "$vs" | sed 's/:/%3A/g; s/+/%2B/g; s#/#%2F#g; s/=/%3D/g')"
    resp="$(curl -s -c "$COOKIES" -b "$COOKIES" -H 'Content-Type: application/x-www-form-urlencoded' --data "$body" "$BASE/$view.xhtml")"
    i=$((i+1))
  done
}

echo "[$LABEL] warmup..."
for p in "${GETS[@]}"; do curl -s -o /dev/null "$BASE/$p.xhtml?w=[1-$GW]"; done
postback form-inputs $(( PB/4 ))
echo "[$LABEL] reset..."; curl -s "$BASE/perf-stats?reset=1" >/dev/null
echo "[$LABEL] measure (get=$GM, postback=$PB)..."
for p in "${GETS[@]}"; do curl -s -o /dev/null "$BASE/$p.xhtml?m=[1-$GM]"; done
postback form-inputs "$PB"
echo "[$LABEL] === results ==="
curl -s "$BASE/perf-stats"
