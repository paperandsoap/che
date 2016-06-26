/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.ext.java.client.event.ResolvingProjectEvent;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.IN_PROGRESS;
import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.RESOLVED;

public class JavaReconcilerStrategy implements ReconcilingStrategy, ResolvingProjectStateHolder.ResolvingProjectStateListener {

    private final TextEditorPresenter<?>          editor;
    private final JavaCodeAssistProcessor         codeAssistProcessor;
    private final AnnotationModel                 annotationModel;
    private final SemanticHighlightRenderer       highlighter;
    private final ResolvingJavaProjectStateHolder resolvingJavaProjectStateHolder;
    private final JavaReconcileClient             client;
    private       VirtualFile                     file;

    @AssistedInject
    public JavaReconcilerStrategy(@Assisted @NotNull final TextEditorPresenter<?> editor,
                                  @Assisted final JavaCodeAssistProcessor codeAssistProcessor,
                                  @Assisted final AnnotationModel annotationModel,
                                  final JavaReconcileClient client,
                                  final SemanticHighlightRenderer highlighter,
                                  final ResolvingJavaProjectStateHolder resolvingJavaProjectStateHolder) {
        this.editor = editor;
        this.client = client;
        this.codeAssistProcessor = codeAssistProcessor;
        this.annotationModel = annotationModel;
        this.highlighter = highlighter;
        this.resolvingJavaProjectStateHolder = resolvingJavaProjectStateHolder;

        ResolvingProjectState resolvingProjectState = resolvingJavaProjectStateHolder.getState();
        if (resolvingProjectState == IN_PROGRESS) {
            disableReconciler();
        }

        switch (state) {
            case IN_PROGRESS:

                break;
            case RESOLVED:
                parse();
                break;
            default:
                break;
        }
    }

    @Override
    public void setDocument(final Document document) {
        file = editor.getEditorInput().getFile();
        highlighter.init(editor.getHasTextMarkers(), document);
    }

    @Override
    public void reconcile(final DirtyRegion dirtyRegion, final Region subRegion) {
        parse();
    }

    public void parse() {
        String fqn = JavaSourceFolderUtil.getFQNForFile(file);
        client.reconcile(file.getProject().getProjectConfig().getPath(), fqn, new JavaReconcileClient.ReconcileCallback() {
            @Override
            public void onReconcile(ReconcileResult result) {
                if (resolvingJavaProjectStateHolder.getState() == IN_PROGRESS) {
                    disableReconciler();
                    return;
                }

                if (result == null) {
                    return;
                }
                doReconcile(result.getProblems());
                highlighter.reconcile(result.getHighlightedPositions());
            }
        });
    }


    @Override
    public void reconcile(final Region partition) {
        parse();
    }

    public VirtualFile getFile() {
        return file;
    }

    private void doReconcile(final List<Problem> problems) {
        if (resolvingJavaProjectStateHolder.getState() == RESOLVED) {
            codeAssistProcessor.enableCodeAssistant();
        }

        if (this.annotationModel == null) {
            return;
        }
        ProblemRequester problemRequester;
        if (this.annotationModel instanceof ProblemRequester) {
            problemRequester = (ProblemRequester)this.annotationModel;
            problemRequester.beginReporting();
        } else {
            editor.setErrorState(EditorWithErrors.EditorState.NONE);
            return;
        }
        try {
            boolean error = false;
            boolean warning = false;
            for (Problem problem : problems) {

                if (!error) {
                    error = problem.isError();
                }
                if (!warning) {
                    warning = problem.isWarning();
                }
                problemRequester.acceptProblem(problem);
            }
            if (error) {
                editor.setErrorState(EditorWithErrors.EditorState.ERROR);
            } else if (warning) {
                editor.setErrorState(EditorWithErrors.EditorState.WARNING);
            } else {
                editor.setErrorState(EditorWithErrors.EditorState.NONE);
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            problemRequester.endReporting();
        }
    }

    private void disableReconciler() {
        codeAssistProcessor.disableCodeAssistant();
        doReconcile(Collections.<Problem>emptyList());
        highlighter.reconcile(Collections.<HighlightedPosition>emptyList());
    }

    @Override
    public void closeReconciler() {
    }

    @Override
    public void onResolvingProjectStateChanged(ResolvingProjectState state) {

    }
}
