@rem ***************************************************************************
@rem Copyright (c) 2016 comtel inc.
@rem
@rem Licensed under the Apache License, version 2.0 (the "License"); 
@rem you may not use this file except in compliance with the License. 
@rem You may obtain a copy of the License at:
@rem
@rem     http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
@rem WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
@rem License for the specific language governing permissions and limitations
@rem under the License.
@rem ***************************************************************************
javapackager -deploy -native exe -outdir installer -outfile jfxvnc-app -name jfxvnc -appclass org.jfxvnc.ui.VncClientApp -v -srcfiles jfxvnc-app.jar -BclassPath=.