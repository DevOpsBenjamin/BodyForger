# Training statistics

## 1. What counts

Every figure reads **completed sets only**. A set that was planned but never validated is not
training, and counting it would inflate the numbers an athlete uses to judge progress — the
one place where a flattering error is worse than none.

Sessions that were never closed carry no duration, so they add nothing to the hours trained.

## 2. Estimated one-rep max

A personal record is the best *estimated* one-rep max on an exercise, not the heaviest load
lifted. Five reps at 100 kg is a better performance than one rep at 105 kg, and only an
estimate says so.

The estimate uses **Epley's formula**:

```
1RM ≈ weight × (1 + reps / 30)
```

It is one of several in circulation — Brzycki, Lombardi and others give slightly different
numbers — and none is exact. What matters here is that the same formula is applied to every
set, so records stay comparable with each other over time. Changing it would rewrite the whole
history's ranking, which is why it lives in one named constant.

A set with no load sets no record: a bodyweight exercise has nothing to compare.

## 3. What is not computed

**Active calories.** Nothing measures them: `WorkoutSession.activeCaloriesKcal` is never
filled. The card that displayed them has been removed rather than showing an estimate that
would read as a measurement. It comes back when Health Connect or the watch provides one.

**Average heart rate.** Same reasoning — see #44.
