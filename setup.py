import os
import shutil
import subprocess

from setuptools import setup
from setuptools.command.build_py import build_py
from setuptools.command.sdist import sdist

ROOT = os.path.dirname(os.path.abspath(__file__))
JAR_NAME = "cdis2java-999-SNAPSHOT.jar"
TARGET_JAR = os.path.join(ROOT, "target", JAR_NAME)


def maven_command():
    return os.environ.get("MAVEN", "mvn").split()


def run_maven_build():
    cmd = maven_command() + [
        "-B",
        "-q",
        "-Dmaven.test.skip=true",
        "package",
    ]
    result = subprocess.run(cmd, cwd=ROOT)
    if result.returncode != 0:
        raise RuntimeError(f"Maven build failed with exit code {result.returncode}")
    if not os.path.isfile(TARGET_JAR):
        raise RuntimeError(f"Maven build did not produce {TARGET_JAR}")


class BuildPyMaven(build_py):
    def run(self):
        run_maven_build()
        super().run()
        self.embed_jar()

    def embed_jar(self):
        dest_dir = os.path.join(self.build_lib, "cdis2java")
        os.makedirs(dest_dir, exist_ok=True)
        shutil.copy2(TARGET_JAR, os.path.join(dest_dir, JAR_NAME))


class SDistMaven(sdist):
    def make_distribution(self):
        self.filelist.append("pom.xml")
        for root, _, files in os.walk(os.path.join("src", "main")):
            for name in files:
                self.filelist.append(os.path.join(root, name))
        super().make_distribution()


setup(cmdclass={"build_py": BuildPyMaven, "sdist": SDistMaven})
