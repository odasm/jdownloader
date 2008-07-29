//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team jdownloader@freenet.de
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.decrypt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import jd.plugins.DownloadLink;
import jd.plugins.HTTP;
import jd.plugins.PluginForDecrypt;
import jd.plugins.RequestInfo;

public class RsXXXBlog extends PluginForDecrypt {
    static private final String host = "rs.xxx-blog.org";

    private String version = "1.0.0.2";
    static private final Pattern patternSupported = Pattern.compile("http://[\\w\\.]*?xxx-blog\\.org/[a-zA-Z0-9]{1,4}-[a-zA-Z0-9]{10,40}/.*", Pattern.CASE_INSENSITIVE);

    public RsXXXBlog() {
        super();
        // steps.add(new PluginStep(PluginStep.STEP_DECRYPT, null));
        // currentStep = steps.firstElement();
        default_password.add("xxx-blog.dl.am");
        default_password.add("xxx-blog.org");
    }

    @Override
    public String getCoder() {
        return "JD-Team";
    }

    @Override
    public String getPluginName() {
        return host;
    }

    @Override
    public Pattern getSupportedLinks() {
        return patternSupported;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getPluginID() {
        return host + "-" + version;
    }

    @Override
    public boolean doBotCheck(File file) {
        return false;
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(String parameter) {
        // //if (step.getStep() == PluginStep.STEP_DECRYPT) {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        try {
            parameter = parameter.substring(parameter.lastIndexOf("http://"));
            URL url = new URL(parameter.replaceFirst("http://[\\w\\.]*?xxx-blog.org", "http://xxx-blog.org/frame"));
            RequestInfo requestInfo = HTTP.getRequestWithoutHtmlCode(url, null, null, false);
            decryptedLinks.add(this.createDownloadlink(requestInfo.getLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // step.setParameter(decryptedLinks);
        return decryptedLinks;
    }
}
