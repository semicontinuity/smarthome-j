package gearbox.server.http.xlightweb;

import org.xlightweb.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

// TODO: XlightWeb's file handler maps files to memory, that are not unmapped immediately.
@Resource
public class FileRequestHandler implements IHttpRequestHandler {
    protected File fileBase;


    @SuppressWarnings({"UnusedDeclaration"})
    public void setPath(final File path) {
        this.fileBase = path;
    }


    public void onRequest(final IHttpExchange exchange) throws IOException {

        IHttpRequest request = exchange.getRequest();

        boolean isGET = request.getMethod().equalsIgnoreCase("GET");
        boolean isHEAD = request.getMethod().equalsIgnoreCase("HEAD");

        // only GET or HEAD is supported by this handler
        if (isGET || isHEAD) {

            String requestURI = URLDecoder.decode(request.getRequestURI(), "utf-8");
            int ctxLength = request.getContextPath().length() + request.getRequestHandlerPath().length();


            if (requestURI.length() > ctxLength) {

                String filepath = requestURI.substring(ctxLength, requestURI.length());

                // file defined?
                if (filepath.length() > 0) {

                    // converting slash to file system's one
                    filepath = filepath.replaceAll("[/\\\\]+", "\\" + File.separator);

                    // create native path
                    String path = fileBase.getAbsolutePath() + filepath;

                    // removing tailing file separator
                    if (path.endsWith(File.separator)) {
                        path = path.substring(0, path.length() - 1);
                    }

                    final File file = new File(path);

                    // does file exits?
                    if (file.exists()) {

                        // is file?
                        if (file.isFile()) {
                            final String ifModifiedSinceRequestHeader = request.getHeader("If-Modified-Since");

                            if ((ifModifiedSinceRequestHeader != null) && (!HttpUtils.isAfter(ifModifiedSinceRequestHeader, file.lastModified()))) {
                                final HttpResponse response = new HttpResponse(304);
//                                enhanceFoundResponseHeader((HttpResponseHeader) response.getResponseHeader(), file.lastModified());
//                                if (LOG.isLoggable(Level.FINE)) {
//                                    LOG.fine(filepath + " requested. returning not modified");
//                                }

                                exchange.send(response);
                                return;
                            }


                            final HttpResponseHeader responseHeader = new HttpResponseHeader(200);
//                            enhanceFoundResponseHeader(responseHeader, file.lastModified());
//                            if (LOG.isLoggable(Level.FINE)) {
//                                LOG.fine(filepath + " requested. returning data");
//                            }


                            String range = request.getHeader("Range");

                            HttpResponse response = new HttpResponse(responseHeader, file/*, range*/);
                            if (isHEAD) {
                                response = new HttpResponse(response.getResponseHeader());
                            }

                            exchange.send(response);

//                            countFound++;

                            // ... on, it is a directory
                        } else {
                            handleNotFound(exchange, request, file);
                        }

                        // file does not exit
                    } else {
                        handleNotFound(exchange, request, file);
                    }
                }

                // no file defined
            } else {
                exchange.sendError(404, request.getRequestURI() + " not found");
            }
        }
        else {
            exchange.forward(request, new HttpResponseHandler(exchange));
        }
    }


    private void enhanceFoundResponseHeader(HttpResponseHeader responseHeader, long lastModified) {
        responseHeader.setDate(System.currentTimeMillis());
//        if (expireSec == null) {
            responseHeader.setLastModifiedHeader(lastModified);
//        } else {
//            responseHeader.setExpireHeaders(expireSec);
//        }
    }


    private void handleNotFound(IHttpExchange exchange, IHttpRequest request, File file) throws IOException {

//        countNotFound++;

//        if ((isShowDirectoryTree) &&
//                (file.isDirectory() &&
//                        (fileBase.getAbsolutePath().length() <= file.getAbsolutePath().length()))) {
//            String body = printDirectoryTree(request, file);
//            exchange.send(new HttpResponse(200, "text/html", body));
//            return;
//        }

        exchange.forward(request, new HttpResponseHandler(exchange));
    }


    private static final class HttpResponseHandler implements IHttpResponseHandler {

        private IHttpExchange exchange = null;

        public HttpResponseHandler(IHttpExchange exchange) {
            this.exchange = exchange;
        }

        public void onResponse(IHttpResponse response) throws IOException {
            exchange.send(response);
        }

        public void onException(IOException ioe) {
            exchange.sendError(500);
        }
    }
}
