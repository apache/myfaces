# benchmark — Jetty container benchmark (MyFaces vs Mojarra)

Standalone WAR (NOT part of the myfaces reactor) that runs a few Facelets pages in a real servlet
container via the `jetty-ee10-maven-plugin`, with a `PhaseListener` recording per-phase wall-clock
time. Lets MyFaces 5.0 and Mojarra 5.0.0-M3 be compared on identical pages, and MyFaces before/after
an impl change.

## Prereqs

Install the MyFaces build you want to measure into the local repo first:

```
mvn -pl api,impl -am install -DskipTests
```

(Repeat after each impl change. The `-Pmojarra` run doesn't need this.)

## Run the server

```
cd benchmark
mvn org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:run              # MyFaces (default)
mvn -Pmojarra org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:run   # Mojarra 5.0.0-M3
```

Server comes up on http://localhost:8080/ . Pages: `table-readonly` / `repeat-readonly` /
`foreach-readonly` / `nested-readonly` (GET render), and `form-inputs` (full-lifecycle postback).
Stats endpoint: `GET /perf-stats` (text dump), `GET /perf-stats?reset=1` (clear).

## Drive it (per-phase timing)

`curl` URL-range globbing fires many keep-alive requests cheaply:

```
# warmup
curl -s -o /dev/null "http://localhost:8080/table-readonly.xhtml?w=[1-300]"
curl -s -o /dev/null "http://localhost:8080/repeat-readonly.xhtml?w=[1-300]"
# reset accumulators after warmup
curl -s "http://localhost:8080/perf-stats?reset=1"
# measured run
curl -s -o /dev/null "http://localhost:8080/table-readonly.xhtml?m=[1-3000]"
curl -s -o /dev/null "http://localhost:8080/repeat-readonly.xhtml?m=[1-3000]"
# read per-phase avg_us
curl -s "http://localhost:8080/perf-stats"
```

Compare the `avg_us` for RENDER_RESPONSE (and RESTORE_VIEW) per viewId between MyFaces and Mojarra,
and between MyFaces before/after an impl change.

## Notes / caveats

- Both impls target Jakarta Faces 5.0 (MyFaces 5.0-SNAPSHOT vs Mojarra 5.0.0-M3), so it's a same-spec
  comparison. Run both in the same thermal window and take the best-of-N — a busy machine skews results.
- GET readonly pages exercise RESTORE_VIEW + RENDER_RESPONSE only. `form-inputs` is a full-lifecycle
  postback (all six phases); driving it needs a ViewState round-trip and the impl-specific submit
  marker (`form_SUBMIT` for MyFaces, `form` for Mojarra) — submit every hidden field to stay agnostic.
- `PROJECT_STAGE=Production`; server-side per-phase time is what's measured, so client/curl speed is
  irrelevant as long as enough requests are sent.
