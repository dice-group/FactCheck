package api;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoDemo;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.nlp.stanford.CoreNLPClient;
import org.dice.factcheck.nlp.stanford.impl.CoreNLPLocalClient;
import org.dice.factcheck.nlp.stanford.impl.CoreNLPServerClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationStartup
        implements ApplicationListener<ApplicationReadyEvent> {
    public static CoreNLPClient corenlpClient;

    /**
     * This method is executed when server is started
     * defacto.ini configuration file is loaded
     * CoreNLP is loaded and kept ready to use later
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        Defacto.init();

        if(Defacto.DEFACTO_CONFIG.getBooleanSetting("corenlp", "USE_SERVER"))
        {
            corenlpClient = new CoreNLPServerClient();
        }
        else
        {
            corenlpClient = new CoreNLPLocalClient();
        }

        return;
    }

}
