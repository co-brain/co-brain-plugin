import re
import sys
import getopt
from os import popen
from math import trunc


def get_changed_lines(file_path):
    changed_lines = popen("git diff -U0 {}".format(file_path)).read()
    changed_lines = changed_lines.split("\n")
    changed_lines = list(filter(lambda x: re.match(r"@@ ([-+][0-9]+[,0-9]* )+@@", x), changed_lines))
    changes = []
    for lines in changed_lines:
        lines = lines.split(" ")[1:3]
        lines = tuple(map(lambda x: float(x.replace(",", ".")), lines))
        changes.append(lines)
    return changes


def transform_line_assignment(line_assignment):
    start_line = int(trunc(abs(line_assignment)))
    affected_line = int(round(abs(line_assignment) - start_line, 1)*10)
    return start_line, affected_line


def reconstruct_remote_line_number(changes, snippet_lines):
    """
    Changes: List of tuples with (Head Line-Number, Local Line Number)
    Local_Lines: Tuple with (Start line of snippet, Stop line of snippet)
    """
    # Take all changes that happened before the snippet in the local version
    changes_with_impact = list(filter(lambda x: abs(snippet_lines[0]) >= abs(x[1]), changes))
    # If there are no changes before the snippet return snippet line numbers
    if len(changes_with_impact) == 0:
        return snippet_lines

    # If there are changes before the snippet calculate snippet line numbers in origin based on previous changes
    elif len(changes_with_impact) <= len(changes):
        snippet_start_line, snippet_stop_line = snippet_lines
        snippet_length = snippet_stop_line - snippet_start_line
 
        origin_change_start, origin_affected = transform_line_assignment(changes_with_impact[-1][0])
        local_change_start, local_affected = transform_line_assignment(changes_with_impact[-1][1])
      
        # Calculate difference between start of snippet and start of changes (including by changes affected lines)
        diff_start_to_snippet = 0
        if origin_affected:
            diff_start_to_snippet = snippet_start_line + origin_affected - local_change_start - 1
        elif local_affected:
            diff_start_to_snippet = snippet_start_line - local_affected - local_change_start + 1
        else:
            pass
      
        actual_snippet_start = origin_change_start + diff_start_to_snippet
        actual_snippet_stop = origin_change_start + diff_start_to_snippet + snippet_length
        return actual_snippet_start, actual_snippet_stop


def get_latest_commit():
    commit = popen("git log origin | head -n 1").read()
    commit_id = commit.split(" ")[-1].strip()
    return commit_id


def get_origin_url():
    url = popen("git remote get-url origin").read()
    if "@" in url:
        url = url.split("@")[-1]
    if ":" in url:
        url = url.replace(":", "/")
    url = url.replace(".git", "").strip()
    return url


def create_remote_url(path, start_line, stop_line):
    commit_id = get_latest_commit()
    origin_url = get_origin_url()
    return "{}/blob/{}/{}#L{}-{}".format(origin_url, commit_id, path, start_line, stop_line)


def get_current_user():
    email = popen("git config --list | grep user.email").read().split("=")[1].strip()
    name = popen("git config --list | grep user.name").read().split("=")[1].strip()
    return name, email


def blame(path, snippet_start, snippet_length):
    blaming_mail_raw = popen("git blame {} -e -L {},+{} | sed -n '1p'".format(path, snippet_start, snippet_length)).read()
    blaming_mail_raw = blaming_mail_raw.split("(<")[1]
    blaming_mail = blaming_mail_raw.split(">")[0].strip()
    blaming_name_raw = popen("git blame {} -L {},+{} | sed -n '1p'".format(path, snippet_start, snippet_length)).read()
    blaming_name_raw = blaming_name_raw.split("(")[1]
    blaming_name = re.split(" [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]", blaming_name_raw)[0].strip()
    return blaming_name, blaming_mail


def main(argv):
    popen("touch asdf.txt")
    path = ""
    start_line = 0
    stop_line = 0
    try:
        opts, argv = getopt.getopt(argv, "hp:b:e:", ["path=", "begin=", "end="])
    except getopt.GetoptError:
        print("diff.py -p <path> -b <begin> -e <end>")
        sys.exit(2)
    for opt, arg in opts:
        if opt == "-h":
            print("diff.py -p <path> -b <begin> -e <end>")
            sys.exit()
        elif opt in ("-p", "--path"):
            path = arg
        elif opt in ("-b", "--begin"):
            start_line = int(arg)
        elif opt in ("-e", "--end"):
            stop_line = int(arg.strip())

    changes = get_changed_lines(path)
    origin_start, origin_stop = reconstruct_remote_line_number(changes, (start_line, stop_line))
    blame_name, blame_mail = blame(path, origin_start, origin_stop-origin_start)
    url = create_remote_url(path, origin_start, origin_stop)
    curr_name, curr_mail = get_current_user()
    print(url)
    print(curr_name)
    print(curr_mail)
    print(blame_name)
    print(blame_mail)


if __name__ == "__main__":
    main(sys.argv[1:])
