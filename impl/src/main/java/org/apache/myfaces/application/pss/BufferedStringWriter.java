/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.myfaces.application.pss;

import org.apache.myfaces.application.jsp.JspViewHandlerImpl;
import org.apache.myfaces.application.MyfacesStateManager;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlLinkRendererBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.application.StateManager;
import java.io.Writer;
import java.io.IOException;

/**
 * @author Martin Haimberger
 */
public class BufferedStringWriter extends FastStringWriter {

        private static final Log log = LogFactory.getLog(BufferedStringWriter.class);

        public BufferedStringWriter(FacesContext context, int initialCapcity) {
            super(initialCapcity);
        }

        /**
         * flushes the content of this writer to the given writer.
         * @param writer the content of this writer is written to the given writer
         * @throws IOException IOException
         */
        public void flushToWriter(Writer writer) throws IOException {


                FacesContext facesContext = FacesContext.getCurrentInstance();
                StateManager stateManager = facesContext.getApplication().getStateManager();
                StateManager.SerializedView serializedView
                        = stateManager.saveSerializedView(facesContext);
                if (serializedView != null)
                {
                    //until now we have written to a buffer
                    ResponseWriter bufferWriter = facesContext.getResponseWriter();
                    bufferWriter.flush();
                    //now we switch to real output
                    ResponseWriter realWriter = bufferWriter.cloneWithWriter(writer);
                    facesContext.setResponseWriter(realWriter);

                    String bodyStr = _buffer.toString();
                    //if ( stateManager.isSavingStateInClient(facesContext) )
                    //{
                        int form_marker = bodyStr.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER);
                        int url_marker = bodyStr.indexOf(HtmlLinkRendererBase.URL_STATE_MARKER);
                        int lastMarkerEnd = 0;
                        while (form_marker != -1 || url_marker != -1)
                        {
                            if (url_marker == -1 || (form_marker != -1 && form_marker < url_marker))
                            {
                                //replace form_marker
                                realWriter.write(bodyStr, lastMarkerEnd, form_marker - lastMarkerEnd);
                                stateManager.writeState(facesContext, serializedView);
                                lastMarkerEnd = form_marker + JspViewHandlerImpl.FORM_STATE_MARKER_LEN;
                                form_marker = bodyStr.indexOf(JspViewHandlerImpl.FORM_STATE_MARKER, lastMarkerEnd);
                            }
                            else
                            {
                                //replace url_marker
                                realWriter.write(bodyStr, lastMarkerEnd, url_marker - lastMarkerEnd);
                                if (stateManager instanceof MyfacesStateManager)
                                {
                                    ((MyfacesStateManager)stateManager).writeStateAsUrlParams(facesContext,
                                                                                              serializedView);
                                }
                                else
                                {
                                    log.error("Current StateManager is no MyfacesStateManager and does not support saving state in url parameters.");
                                }
                                lastMarkerEnd = url_marker + HtmlLinkRendererBase.URL_STATE_MARKER_LEN;
                                url_marker = bodyStr.indexOf(HtmlLinkRendererBase.URL_STATE_MARKER, lastMarkerEnd);
                            }
                        }
                        realWriter.write(bodyStr, lastMarkerEnd, bodyStr.length() - lastMarkerEnd);
                    /*}
                    else
                    {
                        realWriter.write( bodyStr );
                    } */
                }
                else
                {
                     // Save state in Server Session ... only write out the content
                    ResponseWriter bufferWriter = facesContext.getResponseWriter();
                    bufferWriter.flush();
                    //now we switch to real output
                    ResponseWriter realWriter = bufferWriter.cloneWithWriter(writer);
                    facesContext.setResponseWriter(realWriter);

                    String bodyStr = _buffer.toString();

                    realWriter.write( bodyStr );
                }
        }

        public int length() {
            return _buffer.length();
        }


    }