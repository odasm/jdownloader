//jDownloader - Downloadmanager
//Copyright (C) 2008  JD-Team jdownloader@freenet.de

//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.decrypt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.PluginForDecrypt;

public class RappersIn extends PluginForDecrypt {
    private static final String HOST  = "rappers.in";
    private static final String CODER = "JD-Team";

    private static final Pattern PATTERN_SUPPORTED 	= Pattern.compile("(http://[\\w\\.]*?rappers\\.in/.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_USIDERID  	= Pattern.compile("artist\\.php\\?action=add2favs&amp;id=(\\d+)");
    private static final Pattern PATTERN_NICKNAME 	= Pattern.compile("<title>rappers.in Artistpage von (.+?)</title>");
    private static final Pattern PATTERN_PAGE_INFOS = Pattern.compile(PATTERN_USIDERID.pattern()+"|"+PATTERN_NICKNAME);
    private static final Pattern PATTERN_DURL    	= Pattern.compile("<filename>(.+?)</filename>");
    private static final Pattern PATTERN_TITEL		= Pattern.compile("<title>(.+?)</title>");
    public RappersIn(PluginWrapper wrapper){
        super(wrapper);
    }
    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink param) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        System.out.println("url: "+ param.getCryptedUrl());
        String page = br.getPage(param.getCryptedUrl());
        // Komplexes Parsingkonstrukt, da in einem durchlauf geparst werden soll!
        Regex pageInfos = new Regex(page, PATTERN_PAGE_INFOS);
        String nick   = null;
        String userId = null;
        for(String s:pageInfos.getRow(0)){
            if (s!=null) {
                nick = s;
                break;
            }
        }
        for(String s:pageInfos.getRow(1)){
            if (s!=null) {
                userId = s;
                break;
            }
        }
        //Next step, laden & parsen der playlist.xml
        if (userId!=null) {
            StringBuilder sb = new StringBuilder("http://www.rappers.in/artistplaylist_main-");
            sb.append(userId);
            sb.append("-1808.xml?281");
            page = br.getPage(sb.toString());
            String[] dUrls = new Regex(page,PATTERN_DURL).getColumn(0);
            String[] titel = new Regex(page,PATTERN_TITEL).getColumn(0);
            assert titel.length==dUrls.length:"ungültiges xml";
            parseTitelNames(titel, nick);
            FilePackage fp = new FilePackage();
            fp.setName("rappers.in - "+ nick);
            for(int i=0;i<dUrls.length;i++){
                DownloadLink dlLink = createDownloadlink(dUrls[i].replaceAll("http://", "httpRappersIn://").replaceAll("rappers.in","viaRappersIn"));
                dlLink.setStaticFileName(titel[i]+".mp3");
                dlLink.setFilePackage(fp);
                System.out.println(dlLink.getDownloadURL());
                decryptedLinks.add(dlLink);


            }
        }
        return decryptedLinks;
    }
    private void parseTitelNames(String[] titel, String nick){
        for (int i = 0; i < titel.length; i++) {
            titel[i] = titel[i].replaceAll("\\+", " ");
            titel[i] = titel[i].replaceAll("%28", "(");
            titel[i] = titel[i].replaceAll("%29", ")");
            titel[i] = titel[i].replaceAll("%\\d+C", "");
            if (nick!=null&&!titel[i].contains("-")) {
                titel[i] = nick + " - " + titel[i];
            }
        }
    }

    @Override
    public String getCoder() {
        return CODER;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public Pattern getSupportedLinks() {
        return PATTERN_SUPPORTED;
    }

    @Override
    public String getVersion() {
        String ret = new Regex("$Revision: 2798 $", "\\$Revision: ([\\d]*?) \\$").getMatch(0);
        return ret == null ? "0.0" : ret;
    }
}
