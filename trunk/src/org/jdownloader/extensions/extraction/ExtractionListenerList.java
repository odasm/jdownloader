//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package org.jdownloader.extensions.extraction;

import java.awt.Color;
import java.io.File;
import java.util.logging.Logger;

import jd.controlling.JDController;
import jd.event.ControlEvent;
import jd.gui.UserIO;

import org.jdownloader.extensions.extraction.translate.T;

/**
 * Updates the Extractionprogess for archives in the downloadlist
 * 
 * @author botzi
 * 
 */
public class ExtractionListenerList implements ExtractionListener {
    private Logger              logger;
    private ExtractionExtension ex;

    ExtractionListenerList() {
        this.ex = ExtractionExtension.getIntance();
    }

    public void onExtractionEvent(ExtractionEvent event) {
        ExtractionController controller = event.getCaller();

        // if (controller.getProgressController() != null) return;

        // Falls der link entfernt wird während dem entpacken
        // if (controller.getArchiv().getFirstArchiveFile().getFilePackage() ==
        // FilePackage.getDefaultFilePackage() &&
        // controller.getProgressController() == null) {
        // logger.warning("LINK GOT REMOVED_: " +
        // controller.getArchiv().getFirstArchiveFile());
        // ProgressController progress = new
        // ProgressController(T._.plugins_optional_extraction_progress_extractfile(controller.getArchiv().getFirstArchiveFile().getFileOutput()),
        // 100, ex.getIconKey());
        // controller.setProgressController(progress);
        // }

        switch (event.getType()) {
        case QUEUED:

            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_queued());

            break;
        case EXTRACTION_FAILED:
            for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                if (link == null) continue;

                if (controller.getException() != null) {

                    link.setStatus(ArchiveFile.Status.ERROR);
                    link.setMessage("Extract failed: " + controller.getException().getMessage());

                } else {
                    link.setStatus(ArchiveFile.Status.ERROR);
                    link.setMessage("Extract failed");

                }
            }

            for (File f : controller.getArchiv().getExtractedFiles()) {
                if (f.exists()) {
                    if (!f.delete()) {
                        logger.warning("Could not delete file " + f.getAbsolutePath());
                    }
                }
            }

            controller.getArchiv().setActive(false);
            ex.onFinished(controller);
            break;
        case PASSWORD_NEEDED_TO_CONTINUE:
            // ??
            // //
            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();

            if (ex.getSettings().isAskForUnknownPasswordsEnabled()) {
                String pass = UserIO.getInstance().requestInputDialog(0, T._.plugins_optional_extraction_askForPassword(controller.getArchiv().getFirstArchiveFile().getName()), "");
                if (pass == null || pass.length() == 0) {

                    controller.getArchiv().getFirstArchiveFile().setStatus(ArchiveFile.Status.ERROR);
                    controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_extractfailedpass());
                    ex.onFinished(controller);
                    break;
                }
                controller.getArchiv().setPassword(pass);
            }
            break;
        case START_CRACK_PASSWORD:
            try {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(((10000 * controller.getCrackProgress()) / controller.getPasswordListSize()) / 100.00));
            } catch (Throwable e) {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(0.00d));

            } // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case START:
            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_openingarchive());
            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case OPEN_ARCHIVE_SUCCESS:
            ex.assignRealDownloadDir(controller);
            break;
        case PASSWORD_FOUND:
            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_passfound());
            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            controller.getArchiv().getFirstArchiveFile().setProgress(0, 0, null);
            break;
        case PASSWORT_CRACKING:
            try {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(((10000 * controller.getCrackProgress()) / controller.getPasswordListSize()) / 100.00));
            } catch (Throwable e) {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(0.00d));

            }
            controller.getArchiv().getFirstArchiveFile().setProgress(controller.getCrackProgress(), controller.getPasswordListSize(), Color.GREEN.darker());

            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case EXTRACTING:

            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_extracting2());

            controller.getArchiv().getFirstArchiveFile().setProgress((long) (controller.getProgress() * 100), 10000, Color.YELLOW.darker());

            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case EXTRACTION_FAILED_CRC:
            if (controller.getArchiv().getCrcError().size() != 0) {
                for (ArchiveFile link : controller.getArchiv().getCrcError()) {
                    if (link == null) continue;

                    link.setStatus(ArchiveFile.Status.ERROR_CRC);

                }
            } else {
                for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                    if (link == null) continue;
                    link.setMessage(T._.plugins_optional_extraction_error_extrfailedcrc());

                }
            }

            for (File f : controller.getArchiv().getExtractedFiles()) {
                if (f.exists()) {
                    if (!f.delete()) {
                        logger.warning("Could not delete file " + f.getAbsolutePath());
                    }
                }
            }

            controller.getArchiv().setActive(false);
            ex.onFinished(controller);
            break;
        case FINISHED:
            File[] files = new File[controller.getArchiv().getExtractedFiles().size()];
            int i = 0;
            for (File f : controller.getArchiv().getExtractedFiles()) {
                files[i++] = f;
            }
            JDController.getInstance().fireControlEvent(new ControlEvent(controller, ControlEvent.CONTROL_ON_FILEOUTPUT, files));

            for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                if (link == null) continue;
                link.setStatus(ArchiveFile.Status.SUCCESSFUL);

            }

            controller.getArchiv().setActive(false);
            ex.onFinished(controller);
            break;
        case NOT_ENOUGH_SPACE:
            for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                if (link == null) continue;
                link.setStatus(ArchiveFile.Status.ERROR_NOT_ENOUGH_SPACE);
            }

            ex.onFinished(controller);
            break;
        case CLEANUP:
            ex.removeArchive(controller.getArchiv());
            break;
        case FILE_NOT_FOUND:
            if (controller.getArchiv().getCrcError().size() != 0) {
                for (ArchiveFile link : controller.getArchiv().getCrcError()) {
                    if (link == null) continue;
                    link.setStatus(ArchiveFile.Status.ERRROR_FILE_NOT_FOUND);

                }
            } else {
                for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                    if (link == null) continue;
                    link.setMessage(T._.plugins_optional_extraction_filenotfound());

                }
            }

            controller.getArchiv().setActive(false);
            ex.onFinished(controller);
            break;
        }
    }
}