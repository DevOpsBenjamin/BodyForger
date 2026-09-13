# The Haige Scale Protocol

How BodyForger talks to a Huawei / HONOR scale of the Haige family, and what the platform
imposes on top. Code in `core-ble` refers here rather than repeating it; constant names match
the terms used below.

Reverse-engineered from `../BLE/TECH.md` and a production JavaScript implementation, then
corrected against real hardware — §7 lists what only the device could teach.

---

## 1. Discovery

The model lives in the **advertised local name** (`HUAWEI Scale 3 Pro-467`), not in the GAP
device name, which is a generic `HaigeBLE`. Matching on the latter recognises nothing.

No hardware scan filter is used: Android filters on the GAP name or a service UUID, and this
family publishes neither usefully. Sorting happens in software.

The **physical MAC** comes from the native scan and is never typed in. It is also the seed of
the root key (§2), which is what makes pairing possible from a watch.

---

## 2. Cryptography

Three mechanisms, deliberately distinct:

**The root key** derives from the physical MAC through two whitebox passes, and exists only to
carry the session key. It is recomputed each connection and stored nowhere.

**Mutual authentication** exchanges two nonces and two HMAC tokens, distinguished by their
salt so that replaying the received token does not impersonate the other side. Both directions
are verified: the reference implementation computes the scale's expected token and never
compares it, which would let any device announcing the right name receive the athlete's
profile.

**The session key**, drawn per connection, then encrypts payloads in AES-128-CTR with a
16-byte IV in front of the body.

⚠️ **Key material belongs to the model, not the family.** It was only ever read from a Scale 3
Pro. Other models inherit it as a deliberately falsifiable hypothesis: a rejected handshake on
another model *is* the refutation. Note the diagnosis is blind — a wrong root key fails
silently, the scale refusing without saying why, so a refusal is not necessarily a wrong key.

---

## 3. Framing

A proprietary layer above the BLE MTU:

```
+---------+----------+------------+-------------------+---------------+
| magic   | length   | sequence   | payload (0..15 B) | CRC-16 (LE)   |
+---------+----------+------------+-------------------+---------------+
```

Length counts the payload **plus three**. The sequence byte holds the total frame count in its
high nibble and the current index in its low nibble — four bits each, so a payload cannot
exceed sixteen frames of fifteen bytes.

The magic byte carries both direction and encryption: `0xDB` and `0xDC` host to scale, clear
and encrypted; `0xBD` and `0xCD` the other way.

### Two CRCs, one per direction

⚠️ **Frames we send are signed CCITT; frames the scale sends are signed MODBUS.** This is a
field observation, not a theory: brute force over two real authentication frames leaves only
one possible variant, and CCITT is off by thousands.

`TECH.md` §3.2 documents only the first, and the reference implementation never verifies
received frames — it reads length and sequence, reassembles, and never inspects the signature.
It therefore had no occasion to notice.

The CCITT table is frozen in the source but was **generated** from its polynomial, and a test
regenerates it. A hand-transcribed table is a silent error source; openScale carries one with
a typo in it.

### Pre-built commands

Some fixed commands are **already complete frames**, magic and CRC included, and are written
verbatim. Passing them through framing produces a frame inside a frame, which the scale
rejects without a word. The generic query command is one of them, and satisfies neither CRC
variant — it is a captured byte sequence, not a frame we build.

---

## 4. GATT profile

Fifteen characteristics. Twelve are proprietary 128-bit UUIDs, meaningful only to this
firmware. Three are Bluetooth SIG allocations, recognisable by the
`0000XXXX-0000-1000-8000-00805f9b34fb` shape: clock synchronisation is the standard
*Current Time* characteristic, and the two capability characteristics sit in a
member-allocated range. Those three do not depend on the model.

Three protection regimes coexist. Authentication runs in the clear, since nothing is
negotiated yet. The session key transport is protected by the root key — it cannot protect
itself. Everything touching the athlete travels under the session key.

⚠️ **The family indicates, it does not notify.** Only one characteristic truly notifies; the
rest use indication, the acknowledged form. The two are not enabled with the same descriptor
value, and writing one for the other has the subscription rejected with no symptom beyond an
unacknowledged descriptor. The choice is read from each characteristic's properties.

⚠️ **Subscribing is not arming.** The telemetry stream emits nothing until the generic query
command is written to it.

---

## 5. Pairing (mode 1)

Run once. As long as an association exists, weigh-ins use it directly and pairing is never
replayed.

```
1. Connect to the scale found by the scan
2. Handshake (§2)
3. Arm binding mode
4. Engrave the HUID          <- irreversible, one memory slot consumed
5. Athlete steps on          -> the scale answers the tare
6. Clock sync
7. User profile, carrying the tare
8. Disarm binding mode
9-10. Validation reading and acknowledgement
```

**Engraving precedes the weigh-in.** By the time the athlete steps on, the slot is already
taken — which is what makes abandoning harmless, and what forbids starting this sequence
lightly.

The HUID belongs to the athlete, is generated once, and is never regenerated: replaying a
pairing must overwrite the same slot rather than consume a second. Two distinct identifiers
would split the history into two people from the scale's point of view.

**Step 5 is a weight-only reading**: neither the handle nor bare feet, since no impedance is
sought. The tare response opens with a status byte, the weight following it.

⚠️ Without a tare, nothing is recorded and binding mode is disarmed — leaving the scale armed
would strand it in that state. The reference implementation instead proceeds with a zero, or a
stale weight, and writes it into the scale's flash memory.

The validation frame that follows is a bonus, not a condition: nothing establishes that every
device emits one during pairing.

---

## 6. Weigh-in (mode 2)

```
1. Wake the scale with a tap    <- without it, it stays invisible
2. Handshake
3. Clock sync
4. User profile, carrying the last known weight
5. Arm the telemetry stream
6. Athlete steps on, barefoot, gripping the handle if there is one
7. Reading, then acknowledgement
```

The estimated weight in the profile helps the scale frame its measurement. The last reading
serves, the pairing tare as fall-back; failing both, nothing is announced rather than an
invented figure the scale would engrave into its calibration.

The athlete's wait deserves its own timeout — undressing and stabilising takes time, and one
modelled on protocol exchanges would fail on someone merely slow. The invitation stays on
screen for the whole wait: it is the arrival of the frame, and nothing else, that tells us the
athlete stepped on.

Acknowledgement matters: without it the scale stays armed and the next weigh-in waits for it
to time out.

---

## 7. What the platform imposes

Android's raw GATT API exposes details that Bleak and the browser's Bluetooth layer hide, so
none of these appear in the reference implementations:

**One GATT operation at a time.** Writing while a write is in flight fails the second one
silently. Everything goes through a lock, each frame awaiting its callback.

**Responses arrive as notifications**, almost never as write returns — hence subscribing
before writing, or the scale answers into the void.

**Listen before you write.** The notification flow has no buffer: what is emitted before a
collector subscribes exists for nobody. The scale sometimes acknowledges in one millisecond.

**Error 133.** A first connection commonly fails for no abnormal reason, especially on a
device never bonded or one whose scan just stopped. Retrying almost always suffices.

**Scanning blocks connecting.** The radio is busy and `connectGatt` fails without explanation,
so a scan must be genuinely cancelled — not merely hidden in the UI state.

**Scan throttling.** More than five scans in thirty seconds and Android refuses the rest for
half a minute, silently. A throttle, a powered-off adapter and an empty room otherwise look
identical.

---

## 8. Diagnosing

The transport logs every step under the `BodyForgerBle` tag, and dumps the GATT profile the
device actually announces — each characteristic, its properties, whether it carries a
notification descriptor, and its name in our map. That is what shows a profile hypothesis
failing on a given model, rather than leaving it to be inferred from a silent failure.

```
adb logcat -s BodyForgerBle
```

---

## 9. Payload layouts

Both structures are **positional and fixed-size**: a field shifted by one byte does not raise
an error, it produces an absurd value accepted without complaint. Offsets in code are named
after the fields below.

### Telemetry frame (`0x97`), decrypted

Identical at both frame lengths; a short frame simply stops after the heart rate.

| Offset | Type | Field |
| :--- | :--- | :--- |
| `0..1` | `uint16_le / 100` | mass, kg |
| `2..3` | `uint16_le / 10` | body fat, % |
| `4..5` | `uint16_le` | year |
| `6`–`10` | `uint8` | month, day, hour, minute, second |
| `11` | `uint8` | status flags |
| `12..23` | `uint16_le` × 6 | six paths, low frequency, at the model's ohm scale |
| `24..25` | `uint16_le` | heart rate, bpm |
| `26..37` | `uint16_le` × 6 | six paths, high frequency; absent from a short frame |

⚠️ **A zero counter means "not measured"** and is omitted rather than stored — including for
body fat and heart rate. A Pro whose handle was not gripped emits thirty-eight bytes that are
entirely zero apart from the mass, and still acknowledges the reading. **Frame length says
nothing about capability.**

The ohm scale factor belongs to the hardware, not to the protocol: `TECH.md` §6.2 warns it is
not universal across the family.

The status byte at offset 11 is exposed raw. On a Scale 3 Pro it is the ISO weekday, verified
on two distinct days; the only known `M00D` capture carries `0xa0` there, which that reading
does not explain.

### User profile (`0x31`), 69 bytes before encryption

| Offset | Length | Field |
| :--- | :--- | :--- |
| `0..29` | 30 B | HUID, ASCII, zero-padded |
| `30..61` | 32 B | secondary UID, optional, left zero |
| `62` | 1 B | sex: 1 male, 0 female |
| `63` | 1 B | age in years |
| `64..65` | `uint16_le` | height, cm |
| `66..67` | `uint16_le` | weight × 100 |
| `68` | 1 B | profile kind: 0 routine, 2 measurement acknowledgement |

The weight carried is the **last known one**, which helps the scale frame its measurement, and
is left at zero when there is none: saying nothing beats announcing an invented weight the
scale would engrave into its calibration.

### Engraving answer (`0x2D`), decrypted

| Offset | Type | Field |
| :--- | :--- | :--- |
| `0` | `uint8` | status; zero means accepted |
| `1..2` | `uint16_le / 100` | tare, kg |

### Clock (`0x52`)

The standard Bluetooth SIG *Current Time* characteristic: ten bytes, year first, weekday in
ISO convention with Monday as 1.
