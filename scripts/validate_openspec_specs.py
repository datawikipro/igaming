#!/usr/bin/env python3
"""
OpenSpec Specification and Change Validator for SmartBet.guru repository.
Validates all canonical specifications in openspec/specs and active change specifications.
"""

import sys
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
SPECS_DIR = REPO_ROOT / "openspec" / "specs"
CHANGES_DIR = REPO_ROOT / "openspec" / "changes"

def validate_main_spec(spec_file: Path) -> list[str]:
    errors = []
    content = spec_file.read_text(encoding="utf-8")
    lines = content.splitlines()

    # 1. Check title
    if not any(line.startswith("# ") for line in lines):
        errors.append(f"{spec_file.name}: Missing H1 title (# <Title>)")

    # 2. Check Purpose
    if "## Purpose" not in content:
        errors.append(f"{spec_file.name}: Missing '## Purpose' section")
    else:
        purpose_match = re.search(r"## Purpose\s*\n+([^#\n]+)", content)
        if not purpose_match or not purpose_match.group(1).strip():
            errors.append(f"{spec_file.name}: Empty '## Purpose' section")

    # 3. Check Requirements
    if "## Requirements" not in content:
        errors.append(f"{spec_file.name}: Missing '## Requirements' section")

    # 4. Check for forbidden delta headers in canonical specs
    delta_headers = ["## ADDED Requirements", "## MODIFIED Requirements", "## REMOVED Requirements", "## RENAMED Requirements"]
    for dh in delta_headers:
        if dh in content:
            errors.append(f"{spec_file.name}: Contains forbidden delta header '{dh}'")

    # 5. Check Requirements and Scenarios
    reqs = re.findall(r"^### Requirement:\s*(.+)$", content, flags=re.MULTILINE)
    if not reqs:
        errors.append(f"{spec_file.name}: No '### Requirement:' definitions found")

    scenarios = re.findall(r"^#### Scenario:\s*(.+)$", content, flags=re.MULTILINE)
    if not scenarios:
        errors.append(f"{spec_file.name}: No '#### Scenario:' definitions found")

    # Verify WHEN and THEN in scenarios
    when_count = len(re.findall(r"\*\*WHEN\*\*", content))
    then_count = len(re.findall(r"\*\*THEN\*\*", content))
    if when_count == 0 or then_count == 0:
        errors.append(f"{spec_file.name}: Missing **WHEN** / **THEN** clauses in scenarios")

    return errors

def validate_change(change_dir: Path) -> list[str]:
    errors = []
    yaml_file = change_dir / ".openspec.yaml"
    if not yaml_file.exists():
        errors.append(f"{change_dir.name}: Missing .openspec.yaml")
    else:
        yaml_content = yaml_file.read_text(encoding="utf-8")
        if "schema:" not in yaml_content or "task_id:" not in yaml_content:
            errors.append(f"{change_dir.name}/.openspec.yaml: Missing schema or task_id")

    for required_file in ["proposal.md", "design.md", "tasks.md"]:
        f = change_dir / required_file
        if not f.exists() or f.stat().st_size == 0:
            errors.append(f"{change_dir.name}: Missing or empty {required_file}")

    # Check tasks format
    tasks_file = change_dir / "tasks.md"
    if tasks_file.exists():
        t_content = tasks_file.read_text(encoding="utf-8")
        if "- [" not in t_content:
            errors.append(f"{change_dir.name}/tasks.md: No checkbox tasks found")

    # Check delta specs if present
    change_specs_dir = change_dir / "specs"
    if change_specs_dir.exists():
        for delta_file in change_specs_dir.glob("*/spec.md"):
            d_content = delta_file.read_text(encoding="utf-8")
            has_delta_header = any(h in d_content for h in ["## ADDED Requirements", "## MODIFIED Requirements", "## REMOVED Requirements"])
            if not has_delta_header:
                errors.append(f"{delta_file}: Missing delta section (## ADDED/MODIFIED/REMOVED Requirements)")

    return errors

def main():
    print("=" * 60)
    print("🔍 Validating OpenSpec Specifications & Active Change...")
    print("=" * 60)

    total_specs = 0
    all_errors = []

    # 1. Validate all canonical specs
    print("\n[1/2] Checking Canonical Specs in openspec/specs/:")
    for spec_dir in sorted(SPECS_DIR.iterdir()):
        if not spec_dir.is_dir() or spec_dir.name.startswith("."):
            continue
        spec_file = spec_dir / "spec.md"
        if not spec_file.exists():
            all_errors.append(f"Missing spec.md in {spec_dir.relative_to(REPO_ROOT)}")
            continue

        total_specs += 1
        errs = validate_main_spec(spec_file)
        if errs:
            all_errors.extend(errs)
            print(f"  ❌ {spec_dir.name}/spec.md - {len(errs)} error(s)")
        else:
            print(f"  ✅ {spec_dir.name}/spec.md - valid")

    # 2. Validate current change
    current_change = CHANGES_DIR / "plane-4367d642"
    print(f"\n[2/2] Checking Active Change {current_change.name}:")
    if current_change.exists():
        change_errs = validate_change(current_change)
        if change_errs:
            all_errors.extend(change_errs)
            print(f"  ❌ {current_change.name} - {len(change_errs)} error(s)")
        else:
            print(f"  ✅ {current_change.name} - all artifacts valid")
    else:
        print(f"  ⚠️ {current_change.name} not found")

    print("\n" + "=" * 60)
    if all_errors:
        print(f"❌ Validation FAILED with {len(all_errors)} error(s):")
        for err in all_errors:
            print(f"   - {err}")
        sys.exit(1)
    else:
        print(f"✅ OpenSpec validation PASSED: all {total_specs} specifications and change artifacts are valid!")
        print("=" * 60)

if __name__ == "__main__":
    main()
