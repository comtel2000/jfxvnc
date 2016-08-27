/*******************************************************************************
 * Copyright (c) 2016 comtel inc.
 *
 * Licensed under the Apache License, version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package org.jfxvnc.net.rfb.codec.handshaker.event;

public class SecurityResultEvent implements HandshakeEvent {

  private final boolean passed;
  private Throwable throwable;

  public SecurityResultEvent(boolean passed) {
    this.passed = passed;
  }

  public SecurityResultEvent(boolean passed, Throwable t) {
    this.passed = passed;
    this.setThrowable(t);
  }

  public boolean isPassed() {
    return passed;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  @Override
  public String toString() {
    return "SecurityResultEvent [passed=" + passed + (throwable != null ? ", throwable=" + throwable.getMessage() : "") + "]";
  }
}
