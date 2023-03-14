import unittest
import os

class FormatTest(unittest.TestCase):
    def assert_command_success(self, cmd: str) -> None:
        exit_code = os.system(cmd)
        self.assertEqual(exit_code, 0, f"Command ${cmd} failed with exit code {exit_code}")

    def test_scala_fmt(self):
        self.assert_command_success("scalafmt scala --test")