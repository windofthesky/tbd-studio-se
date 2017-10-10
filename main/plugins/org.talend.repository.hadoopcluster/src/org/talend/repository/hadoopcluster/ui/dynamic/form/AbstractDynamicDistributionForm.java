// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.hadoopcluster.ui.dynamic.form;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbstractDynamicDistributionForm extends Composite {

    public AbstractDynamicDistributionForm(Composite parent, int style) {
        super(parent, style);
        this.setLayout(new FillLayout());
    }

    abstract public List<String> checkErrors();

    abstract public boolean isComplete();

    public static Point getNewButtonSize(Button btn) {
        return getNewButtonSize(btn, 6);
    }

    public static Point getNewButtonSize(Button btn, int hPadding) {
        Point btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnSize.x += hPadding * 2;
        return btnSize;
    }

    protected Composite createFormContainer(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        FormLayout formLayout = new FormLayout();
        formLayout.marginTop = 10;
        formLayout.marginBottom = 10;
        formLayout.marginLeft = 10;
        formLayout.marginRight = 10;
        container.setLayout(formLayout);
        return container;
    }

    protected int getAlignVertical() {
        return 12;
    }

    protected int getAlignVerticalInner() {
        return 7;
    }

    protected int getAlignHorizon() {
        return 3;
    }

    protected int getHorizonWidth() {
        return 100;
    }

}
