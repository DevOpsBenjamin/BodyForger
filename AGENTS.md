# Development Directives & Conventions — BodyForger

This document governs the development rules, authentication protocols and security directives for
any agent working on **BodyForger**.

---

## 🛑 1. Fundamental Behaviour Rules (Agent Reliability)

1. **Stop at the first surprise**: if a command, a build or an action fails unexpectedly, do **NOT**
   chain blind workarounds in a burst. Stop, analyse the root cause and explain the situation
   clearly.
2. **Transparency and concise explanations**: always state architectural decisions and technical
   changes clearly, with the reasoning behind them.
3. **Absolute respect for the Master Plan (`PLAN.md`)**: do not introduce superfluous third-party
   libraries, and do not change the domain model without a documented technical justification.
4. **Never run `adb screencap`, and never install or launch the application**: the user runs it
   themselves from Android Studio (`Run ▶️`) and drops their screenshots into `appscreen/` by hand.
   The agent builds and runs unit tests through Gradle only.
5. **No pull request before user validation**: **NEVER** open a pull request (`gh pr create`) before
   the user has tested in Android Studio, reviewed the work and given an explicit go-ahead. The
   agent creates its branch, pushes its commits (`git push origin <branch>`), and adjusts the code
   from the user's feedback.

---

## 🛡️ 2. Git Workflow & Quality (CI / CD)

* **Branch policy**:
  * `main` is protected: no direct push, no deletion, no force-push.
  * All development goes through a feature branch (`feat/...`, `fix/...`, `docs/...`).
  * Pull requests require the CI suite to pass (`./gradlew test` and `./gradlew compileDebugKotlin`).
  * **Squash & merge only**, with the branch deleted automatically after the merge.

---

## 🧩 3. UI Architecture, Modularity & Internationalisation (i18n) Standards

1. **Modularity, split as you go**:
   * Any reusable component, or one past ~40-50 lines, is extracted into `ui/components/`
     (e.g. `CompactNumberInput`, `RoutineSetRow`, `RestTimePickerDialog`).
   * Screen files (`ui/screens/`) stay short and light (< 250 lines).
2. **Numeric input hygiene (zero truncated text)**:
   * **NEVER** force a constrained height (`height(44.dp)`) on a Material 3 `OutlinedTextField` for
     small numbers — it truncates and clips the text vertically.
   * Always use dedicated components built on `BasicTextField`, with absolute vertical centring and
     zero padding.
3. **Internationalisation (i18n)**:
   * Labels live in `res/values/strings.xml` (English by default) and `res/values-fr/strings.xml`
     (French).
   * Use `stringResource(R.string.xxx)` in Compose components.

---

## 🧼 4. Code Hygiene

1. **Language: English everywhere, no exception on GitHub.** Code, comments, documentation,
   **commit messages, issue titles and bodies, pull request descriptions and review comments** are
   written in English. The repository is public and MIT-licensed: a French half would close it to
   anyone who does not read both languages.
   * The only exception, and for a different reason: **interface labels** address the athlete and
     follow the i18n rule of §3.3 — English by default in `strings.xml`, French in `values-fr`.
   * History is not rewritten. Existing French commits and issues stay as they are; the rule applies
     from here on.

2. **No explanatory comment in the code.** A well-chosen name replaces a sentence. What needs a
   paragraph really needs a document: a file under `docs/`, or an ADR if it is a decision. The code
   may point to it in one line, never copy it out.
   * What stays acceptable: one line flagging a trap that cannot be deduced from the code — a
     platform workaround, a mandatory call order, a value imposed by a protocol.
   * What does not: restating what the code already says, or recounting the history of a decision.

3. **No magic values.** Every meaningful literal lives in a constant named for **its use**, not for
   its value. Constants that come from a protocol or a documented model carry **the name the
   documentation uses**, so that the two can be cross-read without translation.
   * Exceptions: `0` and `1` as neutral elements, loop indices, and generated tables whose
     provenance the documentation explains.

4. **File size**: a file past ~250 lines reads badly. Data files are split by domain, components by
   responsibility.

---

## 🧪 5. Test & Data Strategy

* **Local-first architecture**: every data model (Kotlin Room DB, IndexedDB) must be testable
  offline against reproducible workout and BIA weigh-in datasets.
* **BIA mathematical integrity**: the `core-bia` module's tests must validate the DEXA algorithms
  against the reference equations (see `PLAN.md`).
