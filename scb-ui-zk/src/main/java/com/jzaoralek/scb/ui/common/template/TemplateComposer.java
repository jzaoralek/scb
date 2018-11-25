package com.jzaoralek.scb.ui.common.template;

import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Borderlayout;

import com.jzaoralek.scb.ui.common.utils.EventQueueHelper;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper.ScbEvent;
import com.jzaoralek.scb.ui.common.utils.WebUtils;

/**
 * Project: scb-ui-zk
 *
 * Created: 16. 11. 2018
 *
 * @author Ales Wojnar | ales@wojnar.cz | http://ales.wojnar.cz
 */
public class TemplateComposer extends SelectorComposer<Borderlayout>{

    private static final long serialVersionUID = 972165498888754L;

    /**
     * šířka prohlížeče pro zobrazení mobilného menu
     */
    private static final int MOBILE_VIEW_THRESHOLD_WIDTH = 1100;

    @Override
    public void doAfterCompose(Borderlayout comp) throws Exception {
        super.doAfterCompose(comp);
        comp.addEventListener(Events.ON_AFTER_SIZE, e -> onAfterSizeEvent((AfterSizeEvent) e));
    }

    /**
     * 
     * @param e
     */
    private void onAfterSizeEvent(AfterSizeEvent e) {
        if (e.getWidth() < MOBILE_VIEW_THRESHOLD_WIDTH) {
            WebUtils.setSessAtribute(MenuVM.SIDE_MENU_FOLDED, true);
            EventQueueHelper.publish(ScbEvent.SIDE_MENU_FOLD_EVENT, true);
        }
    }
}