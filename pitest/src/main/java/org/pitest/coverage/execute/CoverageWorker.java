/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.coverage.execute;

import static java.util.stream.Collectors.toList;
import static org.pitest.util.Unchecked.translateCheckedException;

import java.util.Comparator;
import java.util.List;

import org.pitest.coverage.CoverageReceiver;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.UnContainer;

public class CoverageWorker {

  private final CoveragePipe   pipe;
  private final List<TestUnit> tests;

  public CoverageWorker(final CoveragePipe pipe, final List<TestUnit> tests) {
    this.pipe = pipe;
    this.tests = tests;
  }

  public void run() {
    try {
      final List<TestUnit> decoratedTests = decorateForCoverage(this.tests,
          this.pipe);

      decoratedTests.sort(testComparator());

      final Container c = new UnContainer();

      final Pitest pit = new Pitest(new ErrorListener());
      pit.run(c, decoratedTests);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private static Comparator<TestUnit> testComparator() {
    return Comparator.comparing(o -> o.getDescription().getQualifiedName());
  }

  private static List<TestUnit> decorateForCoverage(final List<TestUnit> plainTests,
      final CoverageReceiver queue) {
    return plainTests.stream()
            .map(each -> new CoverageDecorator(queue, each))
            .collect(toList());
  }
}
