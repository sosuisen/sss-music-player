"""Writes reachability metadata for every JAAD box class.

JAAD's BoxFactory creates the box classes by reflection, either with no
arguments or with one String. A trace only records the boxes that the traced
files contain, so all of them are listed here.

Usage: python -I jaad-box-metadata.py <jaad jar> <output dir>
"""
import json
import os
import re
import subprocess
import sys
import zipfile

jar, out_dir = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(jar) as z:
    classes = sorted(
        n[:-len(".class")].replace("/", ".")
        for n in z.namelist()
        if n.startswith("net/sourceforge/jaad/mp4/boxes/") and n.endswith(".class")
    )

result = subprocess.run(
    ["javap", "-public", "-classpath", jar, *classes],
    capture_output=True, text=True, check=True,
)
entries = []
current = None
abstract = False
for line in result.stdout.splitlines():
    header = re.match(r"^(public )?(abstract )?(final )?class (\S+)", line)
    if header:
        current = header.group(4)
        abstract = bool(header.group(2))
        continue
    if current is None or abstract:
        continue
    ctor = re.match(r"^\s+public " + re.escape(current) + r"\((.*)\);", line)
    if ctor:
        params = [p.strip() for p in ctor.group(1).split(",") if p.strip()]
        if params in ([], ["java.lang.String"]):
            entries.append({
                "type": current,
                "methods": [{"name": "<init>", "parameterTypes": params}],
            })

os.makedirs(out_dir, exist_ok=True)
with open(os.path.join(out_dir, "reachability-metadata.json"), "w", encoding="utf-8") as f:
    json.dump({"reflection": entries}, f, indent=2)
print(len(entries), "entries")
