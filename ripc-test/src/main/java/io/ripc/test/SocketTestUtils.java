/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ripc.test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketTestUtils {

    public static String read(String host, int port) {
        return read(host, port, null);
    }

    public static String read(String host, int port, String dataToSend) {
        try {
            Socket socket = new Socket(host, port);
            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            if (dataToSend != null) {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeBytes(dataToSend);
            }
            StringBuilder content = new StringBuilder();
            int c = reader.read();
            while (c != -1) {
                content.append((char)c);
                c = reader.read();
            }
            reader.close();
            return content.toString();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
