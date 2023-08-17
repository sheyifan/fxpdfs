import datetime
import subprocess

# Use jlink to generate minimum jre
def link():
    # Create new path for minimum jre by current date
    newName = './jres' + '/' + \
     str(datetime.datetime.now().year) + '-' + \
      str(datetime.datetime.now().month) + "-" + \
       str(datetime.datetime.now().day) + "_" + \
       str(datetime.datetime.now().hour) + "-" + \
       str(datetime.datetime.now().second) + "-" + \
       str(datetime.datetime.now().microsecond)
    # Python CLI
    cmd = ["jlink",
    "--add-modules",
    "javafx.fxml,javafx.controls,javafx.graphics,jdk.charsets,java.sql,java.instrument",
    "--output", newName]

    print(f"Execute command:")
    for param in cmd:
        print(param, end=" ")
    print()

    output = subprocess.run(cmd, capture_output=True)
    # Output log seperately. Decoded by UTF-8
    print("stdout:" + output.stdout.decode("utf-8"))
    print("stderr:" + output.stderr.decode("utf-8"))

if __name__ == '__main__':
    link()
