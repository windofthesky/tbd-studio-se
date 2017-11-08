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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.User;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.dynamic.DynamicFactory;
import org.talend.core.runtime.dynamic.IDynamicConfiguration;
import org.talend.core.runtime.dynamic.IDynamicExtension;
import org.talend.core.runtime.dynamic.IDynamicPlugin;
import org.talend.core.runtime.dynamic.IDynamicPluginConfiguration;
import org.talend.designer.maven.aether.DummyDynamicMonitor;
import org.talend.designer.maven.aether.IDynamicMonitor;
import org.talend.hadoop.distribution.dynamic.DynamicDistributionManager;
import org.talend.hadoop.distribution.dynamic.IDynamicDistributionPreference;
import org.talend.hadoop.distribution.dynamic.adapter.DynamicLibraryNeededExtensionAdaper;
import org.talend.hadoop.distribution.dynamic.adapter.DynamicModuleAdapter;
import org.talend.hadoop.distribution.dynamic.adapter.DynamicModuleGroupAdapter;
import org.talend.hadoop.distribution.dynamic.adapter.DynamicPluginAdapter;
import org.talend.hadoop.distribution.dynamic.comparator.DynamicAttributeComparator;
import org.talend.repository.hadoopcluster.i18n.Messages;
import org.talend.repository.hadoopcluster.ui.dynamic.DynamicBuildConfigurationData;
import org.talend.repository.hadoopcluster.ui.dynamic.DynamicBuildConfigurationData.ActionType;
import org.talend.repository.hadoopcluster.ui.dynamic.DynamicModuleGroupData;
import org.talend.repository.hadoopcluster.ui.dynamic.DynamicModuleGroupWizard;
import org.talend.repository.ui.login.LoginDialogV2;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class DynamicBuildConfigurationForm extends AbstractDynamicDistributionForm {

    private Text dynamicConfigNameText;

    private Button exportConfigBtn;

    private TableViewer baseJarsTable;

    private String originName;

    public DynamicBuildConfigurationForm(Composite parent, int style, DynamicBuildConfigurationData configData,
            IDynamicMonitor monitor) {
        super(parent, style, configData);
        createControl();
        addListeners();
    }

    protected void createControl() {

        Composite parent = this;

        Composite container = createFormContainer(parent);

        int ALIGN_VERTICAL = getAlignVertical();
        int ALIGN_HORIZON = getAlignHorizon();

        Label distriNameLabel = new Label(container, SWT.NONE);
        distriNameLabel.setText(Messages.getString("DynamicBuildConfigurationForm.form.distriName")); //$NON-NLS-1$

        dynamicConfigNameText = new Text(container, SWT.BORDER);

        FormData formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(distriNameLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        dynamicConfigNameText.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(dynamicConfigNameText, 0, SWT.CENTER);
        formData.left = new FormAttachment(0);
        distriNameLabel.setLayoutData(formData);

        baseJarsTable = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        TableViewerColumn indexColumn = new TableViewerColumn(baseJarsTable, SWT.RIGHT);
        indexColumn.getColumn().setText(Messages.getString("DynamicBuildConfigurationForm.baseJars.table.index")); //$NON-NLS-1$
        indexColumn.getColumn().setWidth(50);
        indexColumn.setLabelProvider(new RowNumberLabelProvider());
        TableViewerColumn groupNameColumn = new TableViewerColumn(baseJarsTable, SWT.LEFT);
        groupNameColumn.getColumn().setText(Messages.getString("DynamicBuildConfigurationForm.baseJars.table.groupName")); //$NON-NLS-1$
        groupNameColumn.getColumn().setWidth(300);
        BaseJarTableDetailLabelProvider detailLabelProvider = new BaseJarTableDetailLabelProvider();
        groupNameColumn.setLabelProvider(detailLabelProvider);
        Table table = baseJarsTable.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        baseJarsTable.setContentProvider(new BaseJarTableContentProvider(detailLabelProvider));
        // baseJarsTable.setLabelProvider(new BaseJarTableLabelProvider());

        exportConfigBtn = new Button(container, SWT.PUSH);

        formData = new FormData();
        formData.top = new FormAttachment(dynamicConfigNameText, ALIGN_VERTICAL, SWT.BOTTOM);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(exportConfigBtn, -1 * ALIGN_VERTICAL, SWT.TOP);
        baseJarsTable.getTable().setLayoutData(formData);

        exportConfigBtn.setText(Messages.getString("DynamicBuildConfigurationForm.exportConfigBtn")); //$NON-NLS-1$
        formData = new FormData();
        // formData.top = new FormAttachment(baseJarsTable.getTable(), ALIGN_VERTICAL, SWT.BOTTOM);
        formData.left = new FormAttachment(baseJarsTable.getTable(), 0, SWT.LEFT);
        formData.right = new FormAttachment(baseJarsTable.getTable(), getNewButtonSize(exportConfigBtn, 10).x, SWT.LEFT);
        formData.bottom = new FormAttachment(100);
        exportConfigBtn.setLayoutData(formData);

    }

    private void initData() {

        DynamicBuildConfigurationData dynConfigData = getDynamicBuildConfigurationData();
        IDynamicPlugin dynamicPlugin = dynConfigData.getDynamicPlugin();
        IDynamicPluginConfiguration pluginConfiguration = dynamicPlugin.getPluginConfiguration();
        String name = pluginConfiguration.getName();
        originName = name;
        ActionType actionType = dynConfigData.getActionType();
        if (ActionType.EditExisting.equals(actionType)) {
            if (!isReadonly()) {
                String userName = null;
                RepositoryContext repositoryContext = ProxyRepositoryFactory.getInstance().getRepositoryContext();
                if (repositoryContext != null) {
                    User user = repositoryContext.getUser();
                    if (user != null) {
                        userName = Messages.getString("DynamicBuildConfigurationForm.name.updatedBy.fullNameWithFamilyName", //$NON-NLS-1$
                                user.getFirstName(), user.getLastName());
                    }
                }
                if (StringUtils.isNotEmpty(userName)) {
                    name = name + " " + Messages.getString("DynamicBuildConfigurationForm.name.updatedBy", userName); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }
        dynamicConfigNameText.setText(name);

        initTableViewData(dynamicPlugin);

    }

    private void addListeners() {

        exportConfigBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onExportConfigurationSelected();
                // updateButtons();
            }

        });

    }

    private void onExportConfigurationSelected() {
        DirectoryDialog dirDialog = new DirectoryDialog(getShell());
        String folderPath = dirDialog.open();
        if (StringUtils.isNotEmpty(folderPath)) {
            try {
                IDynamicPlugin dynamicPlugin = getDynamicBuildConfigurationData().getDynamicPlugin();
                IDynamicPluginConfiguration pluginConfiguration = dynamicPlugin.getPluginConfiguration();
                String id = pluginConfiguration.getId();
                String fileName = id + "." + DynamicDistributionManager.DISTRIBUTION_FILE_EXTENSION; //$NON-NLS-1$
                String filePath = folderPath + "/" + fileName; //$NON-NLS-1$

                File file = new File(filePath);
                if (file.exists()) {
                    boolean agree = MessageDialog.openQuestion(getShell(),
                            Messages.getString("DynamicBuildConfigurationForm.exportConfig.dialog.fileExist.title"), //$NON-NLS-1$
                            Messages.getString("DynamicBuildConfigurationForm.exportConfig.dialog.fileExist.message", //$NON-NLS-1$
                                    file.getCanonicalPath()));
                    if (!agree) {
                        return;
                    }
                }

                IDynamicMonitor monitor = new DummyDynamicMonitor();
                DynamicDistributionManager.getInstance().saveUsersDynamicPlugin(dynamicPlugin, filePath, monitor);
                MessageDialog.openInformation(getShell(),
                        Messages.getString("DynamicBuildConfigurationForm.exportConfig.dialog.title"), //$NON-NLS-1$
                        Messages.getString("DynamicBuildConfigurationForm.exportConfig.dialog.message", //$NON-NLS-1$
                                new File(filePath).getCanonicalPath()));
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private void enableJarsTable(boolean enable) {
        // if (isReadonly()) {
        // enable = false;
        // }
        // baseJarsTable.getControl().setEnabled(enable);
        baseJarsTable.getControl().setEnabled(true);
    }

    @Override
    public boolean isComplete() {
        try {
            initData();
            enableUI();
            showMessage(null, WizardPage.INFORMATION);
            return true;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return false;
        } finally {
            exportConfigBtn.setEnabled(true);
        }
    }

    private void enableUI() {
        dynamicConfigNameText.setEditable(!isReadonly());
        enableJarsTable(true);
    }

    private void initTableViewData(IDynamicPlugin dynamicPlugin) {
        if (dynamicPlugin == null) {
            baseJarsTable.setInput(null);
        } else {
            IDynamicExtension libNeededExtension = DynamicPluginAdapter.getLibraryNeededExtension(dynamicPlugin);
            List<IDynamicConfiguration> configurations = libNeededExtension.getConfigurations();
            Iterator<IDynamicConfiguration> iter = configurations.iterator();
            List<IDynamicConfiguration> moduleGroups = new ArrayList<>();
            while (iter.hasNext()) {
                IDynamicConfiguration dynConfig = iter.next();
                if (DynamicModuleGroupAdapter.TAG_NAME.equals(dynConfig.getTagName())) {
                    moduleGroups.add(dynConfig);
                }
            }
            Collections.sort(moduleGroups, new DynamicAttributeComparator());
            baseJarsTable.setInput(moduleGroups);
        }
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    @Override
    public boolean canFinish() {
        if (isComplete()) {
            if (isReadonly()) {
                return false;
            }
            return true;
        }
        return false;
    }

    protected boolean isReadonly() {
        return getDynamicBuildConfigurationData().isReadonly();
    }

    protected class BaseJarTableContentProvider extends ArrayContentProvider {

        private BaseJarTableDetailLabelProvider detailLabelProvider;

        public BaseJarTableContentProvider(BaseJarTableDetailLabelProvider detailLabelProvider) {
            this.detailLabelProvider = detailLabelProvider;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.detailLabelProvider.clearBtns();
            this.detailLabelProvider.setDynamicPlugin(getDynamicBuildConfigurationData().getDynamicPlugin());
            this.detailLabelProvider.setReadonly(isReadonly());
        }

    }

    protected static class BaseJarTableDetailLabelProvider extends ColumnLabelProvider {

        private Map<Object, Composite> compositeMap = new HashMap<Object, Composite>();

        private Map<String, String> mavenUriMap = new HashMap<>();

        private IDynamicPlugin dynamicPlugin;

        private IDynamicPlugin tempDynamicPlugin;

        private DynamicPluginAdapter pluginAdapter;

        private boolean readonly = false;

        @Override
        public String getText(Object element) {
            if (element instanceof IDynamicConfiguration) {
                return getModuleGroupTemplateId(element);
            }
            return element == null ? "" : element.toString();//$NON-NLS-1$
        }

        @Override
        public void update(ViewerCell cell) {

            TableItem item = (TableItem) cell.getItem();
            Composite composite = null;
            Object element = cell.getElement();
            if (compositeMap.containsKey(cell.getElement())) {
                composite = compositeMap.get(cell.getElement());
            } else {
                composite = new Composite((Composite) cell.getViewerRow().getControl(), SWT.NONE);
                composite.setLayout(new FormLayout());
                // composite.setBackground(getBackground(element));
                composite.setBackground(LoginDialogV2.WHITE_COLOR);
                composite.setForeground(getForeground(element));

                String text = getText(element);
                CLabel label = new CLabel(composite, SWT.NONE);
                label.setBackground(LoginDialogV2.WHITE_COLOR);
                label.setText(text);
                Button button = new Button(composite, SWT.PUSH);
                button.setText(Messages.getString("DynamicBuildConfigurationForm.baseJars.table.groupDetails.btn")); //$NON-NLS-1$
                addDetailBtnListener(button, getModuleGroupTemplateId(element));

                FormData formData = new FormData();
                formData.top = new FormAttachment(0);
                formData.bottom = new FormAttachment(100);
                formData.left = new FormAttachment(0);
                formData.right = new FormAttachment(button, -10, SWT.LEFT);
                label.setLayoutData(formData);

                formData = new FormData();
                formData.top = new FormAttachment(0);
                formData.bottom = new FormAttachment(100);
                formData.right = new FormAttachment(100);
                button.setLayoutData(formData);

                compositeMap.put(cell.getElement(), composite);

            }

            // cell.setBackground(getBackground(element));
            cell.setBackground(LoginDialogV2.WHITE_COLOR);
            cell.setForeground(getForeground(element));
            cell.setFont(getFont(element));

            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(composite, item, cell.getColumnIndex());
            editor.layout();
        }

        private String getModuleGroupTemplateId(Object element) {
            return (String) ((IDynamicConfiguration) element).getAttribute(DynamicModuleGroupAdapter.ATTR_GROUP_TEMPLATE_ID);
        }

        private String getModuleGroupRuntimeId(Object element) {
            return (String) ((IDynamicConfiguration) element).getAttribute(DynamicModuleGroupAdapter.ATTR_ID);
        }

        @Override
        public void dispose() {
            super.dispose();
            clearBtns();
        }

        public void clearBtns() {
            Collection<Composite> values = compositeMap.values();
            if (values != null) {
                for (Composite composite : values) {
                    composite.dispose();
                }
            }
            compositeMap.clear();
            mavenUriMap.clear();
        }

        public void setReadonly(boolean readonly) {
            this.readonly = readonly;
        }

        public boolean isReadonly() {
            return readonly;
        }

        public void setDynamicPlugin(IDynamicPlugin dynPlugin) {

            if (dynPlugin == null) {
                mavenUriMap.clear();
                dynamicPlugin = null;
                tempDynamicPlugin = null;
                pluginAdapter = null;
                return;
            }

            try {
                this.dynamicPlugin = dynPlugin;
                this.tempDynamicPlugin = DynamicFactory.getInstance()
                        .createPluginFromJson(this.dynamicPlugin.toXmlJson().toString());
                IDynamicPluginConfiguration pluginConfiguration = tempDynamicPlugin.getPluginConfiguration();
                String distribution = pluginConfiguration.getDistribution();
                IDynamicDistributionPreference dynamicDistributionPreference = DynamicDistributionManager.getInstance()
                        .getDynamicDistributionGroup(distribution).getDynamicDistributionPreference();
                pluginAdapter = new DynamicPluginAdapter(tempDynamicPlugin, dynamicDistributionPreference);
                pluginAdapter.buildIdMaps();
                Set<String> allModuleIds = pluginAdapter.getAllModuleIds();
                Iterator<String> iter = allModuleIds.iterator();
                while (iter.hasNext()) {
                    String id = iter.next();
                    IDynamicConfiguration module = pluginAdapter.getModuleById(id);
                    String mavenUri = (String) module.getAttribute(DynamicModuleAdapter.ATTR_MVN_URI);
                    mavenUriMap.put(mavenUri, id);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

        private void addDetailBtnListener(Button btn, final String groupTemplateId) {
            btn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    DynamicModuleGroupData groupData = new DynamicModuleGroupData();
                    groupData.setDynamicPlugin(tempDynamicPlugin);
                    groupData.setGroupTemplateId(groupTemplateId);
                    groupData.setPluginAdapter(pluginAdapter);
                    groupData.setMavenUriIdMap(new HashMap<>(mavenUriMap));
                    groupData.setReadonly(isReadonly());
                    DynamicModuleGroupWizard wizard = new DynamicModuleGroupWizard(groupData);
                    WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            wizard);
                    wizardDialog.create();
                    int result = wizardDialog.open();
                    if (result == IDialogConstants.OK_ID) {
                        IDynamicExtension oldLibNeeded = DynamicPluginAdapter.getLibraryNeededExtension(dynamicPlugin);
                        int index = dynamicPlugin.getChildIndex(oldLibNeeded);
                        if (index < 0) {
                            index = 1;
                        }
                        dynamicPlugin.removeExtensions(DynamicLibraryNeededExtensionAdaper.ATTR_POINT);
                        dynamicPlugin.addExtension(index, DynamicPluginAdapter.getLibraryNeededExtension(tempDynamicPlugin));
                    }
                    setDynamicPlugin(dynamicPlugin);
                }

            });
        }

    }

}
