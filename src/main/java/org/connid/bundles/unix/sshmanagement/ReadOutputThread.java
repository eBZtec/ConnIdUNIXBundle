/**
 * Copyright (C) 2011 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connid.bundles.unix.sshmanagement;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.connid.bundles.unix.UnixResult;
import org.identityconnectors.common.logging.Log;
//import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import com.jcraft.jsch.ChannelExec;

public class ReadOutputThread implements Callable<UnixResult> {

    private static final Log LOG = Log.getLog(ReadOutputThread.class);
    private static String PASSWD_PROMPT = "[sudo] password for";
    private InputStream fromServer;
    private InputStream errorStream;
    private ChannelExec execChannel;
    private boolean isRead;

    public ReadOutputThread(InputStream fromServer, InputStream errorStream, ChannelExec execChannel, boolean isRead) {
        this.fromServer = fromServer;
        this.errorStream = errorStream;
        this.execChannel = execChannel;
        this.isRead = isRead;
    }

    @Override
    public UnixResult call() throws Exception {

        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(fromServer));
        StringBuilder buffer = new StringBuilder();
        LOG.ok("Input stream, available {0}", fromServer.available());

        while (true) {
            while (fromServer.available() > 0) {
                while ((line = br.readLine()) != null) {
                    if (isRead) {
                        if (line.contains("Could not chdir to home directory")) {
                            continue;
                        }
                    }
                    if (line.contains(PASSWD_PROMPT)) {
                        line = trimLine(line);

                    }
                    LOG.ok("Reading line: {0}", line);
                    buffer.append(line).append("\n");
                }
            }
            if (execChannel.isClosed()) {
                if(fromServer.available() > 0) continue;
                LOG.ok("Channel for {1} closed: {0}", new Object[]{execChannel.isClosed(), execChannel.getSession().getHost()});
                LOG.ok("exit-status: {0}", execChannel.getExitStatus());
                break;
            }

            try{
                Thread.sleep(1000);
            }catch(Exception ee){}
        }

        LOG.ok("buffer {0}", buffer.toString());

        return new UnixResult(execChannel.getExitStatus(), buffer.toString(), buffer.toString());

    }

    private String trimLine(String line) {

        String[] lineParts = line.split(":");

        line = line.replace(lineParts[0]+":", "");
        return line.trim();
    }

}
