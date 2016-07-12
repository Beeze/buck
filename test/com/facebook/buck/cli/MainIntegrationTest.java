/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static org.hamcrest.Matchers.containsString;

import com.facebook.buck.testutil.integration.DebuggableTemporaryFolder;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.TestDataHelper;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.base.Joiner;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class MainIntegrationTest {

  @Rule
  public DebuggableTemporaryFolder tmp = new DebuggableTemporaryFolder();

  @Test
  public void testBuckNoArgs() throws IOException, InterruptedException {
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "empty_project", tmp);
    workspace.setUp();

    ProjectWorkspace.ProcessResult result = workspace.runBuckCommand();

    result.assertFailure();
    assertThat(
        "When the user does not specify any arguments, the usage information should be displayed",
        result.getStderr(),
        containsString(getUsageString()));
  }

  @Test
  public void testBuckHelp() throws IOException, InterruptedException {
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "empty_project", tmp);
    workspace.setUp();

    ProjectWorkspace.ProcessResult result = workspace.runBuckCommand();

    result.assertFailure();
    assertThat(
        "Users instinctively try running `buck --help`, so it should print usage info.",
        result.getStderr(),
        containsString(getUsageString()));
  }

  @Test
  public void testConfigOverride() throws IOException, InterruptedException {
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "includes_override", tmp);
    workspace.setUp();
    workspace.runBuckCommand("targets", "--config", "buildfile.includes=//includes.py")
        .assertSuccess();
  }

  @Test
  public void testNoRepositoriesConfigOverride() throws IOException, InterruptedException {
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "includes_override", tmp);
    workspace.setUp();
    try {
      workspace.runBuckCommand("targets", "--config", "repositories.secondary=../secondary");
      fail("Did not expect to allow repositories override");
    } catch (HumanReadableException expected) {
      assertEquals("Overriding repository locations from the command line " +
          "is not supported. Please place a .buckconfig.local in the appropriate location and " +
          "use that instead.",
          expected.getMessage());
    }
  }

  @Test
  public void testConfigRemoval() throws IOException, InterruptedException {
    ProjectWorkspace workspace = TestDataHelper.createProjectWorkspaceForScenario(
        this, "includes_removals", tmp);
    workspace.setUp();
    // BUCK file defines `ide` as idea, now lets switch to undefined one!
    // It should produce exception as we want explicit ide setting.
    try {
      workspace.runBuckCommand("project", "--config", "project.ide=");
    } catch (HumanReadableException e) {
      assertThat(e.getHumanReadableErrorMessage(),
          Matchers.stringContainsInOrder(
              "Please specify ide using --ide option " +
                  "or set ide in .buckconfig"));
    } catch (Exception e) {
      // other exceptions are not expected
      throw e;
    }
  }


  private String getUsageString() {
    return Joiner.on('\n').join(
        "buck build tool",
        "usage:",
        "  buck [options]",
        "  buck command --help",
        "  buck command [command-options]",
        "available commands:",
        "  audit       lists the inputs for the specified target",
        "  autodeps    auto-generates dependencies for build rules, where possible",
        "  build       builds the specified target",
        "  cache       makes calls to the artifact cache",
        "  clean       deletes any generated files",
        "  fetch       downloads remote resources to your local machine",
        "  install     builds and installs an application",
        "  machoutils  provides some utils for Mach O binary files",
        "  project     generates project configuration files for an IDE",
        "  publish     builds and publishes a library to a central repository",
        "  query       provides facilities to query information about the target nodes graph",
        "  quickstart  generates a default project directory",
        "  rage        create a defect report",
        "  repl        a shell for interactive experimentation with buck internals",
        "  root        prints the absolute path to the root of the current buck project",
        "  run         runs a target as a command",
        "  server      query and control the http server",
        "  simulate    timed simulation of a build without running the steps",
        "  suggest     suggests a refactoring for the specified build target",
        "  targets     prints the list of buildable targets",
        "  test        builds and runs the tests for the specified target",
        "  uninstall   uninstalls an APK",
        "options:",
        " --help (-h)    : Shows this screen and exits.",
        " --version (-V) : Show version number.",
        "");
  }
}
