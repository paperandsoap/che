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
package org.eclipse.che.ide.api.git;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchCreateRequest;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.CloneRequest;
import org.eclipse.che.api.git.shared.CommitRequest;
import org.eclipse.che.api.git.shared.Commiters;
import org.eclipse.che.api.git.shared.DiffType;
import org.eclipse.che.api.git.shared.FetchRequest;
import org.eclipse.che.api.git.shared.GitUrlVendorInfo;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.MergeRequest;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.git.shared.PullRequest;
import org.eclipse.che.api.git.shared.PullResponse;
import org.eclipse.che.api.git.shared.PushRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteAddRequest;
import org.eclipse.che.api.git.shared.RepoInfo;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.api.git.shared.StatusFormat.PORCELAIN;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * Implementation of the {@link GitServiceClient}.
 *
 * @author Ann Zhuleva
 * @author Valeriy Svydenko
 */
@Singleton
public class GitServiceClientImpl implements GitServiceClient {
    private static final String ADD         = "/git/add";
    private static final String BRANCH      = "/git/branch";
    private static final String CHECKOUT    = "/git/checkout";
    private static final String CLONE       = "/git/clone";
    private static final String COMMIT      = "/git/commit";
    private static final String CONFIG      = "/git/config";
    private static final String DIFF        = "/git/diff";
    private static final String FETCH       = "/git/fetch";
    private static final String INIT        = "/git/init";
    private static final String LOG         = "/git/log";
    private static final String SHOW        = "/git/show";
    private static final String MERGE       = "/git/merge";
    private static final String STATUS      = "/git/status";
    private static final String PUSH        = "/git/push";
    private static final String PULL        = "/git/pull";
    private static final String REMOTE      = "/git/remote";
    private static final String REMOVE      = "/git/rm";
    private static final String RESET       = "/git/reset";
    private static final String COMMITERS   = "/git/commiters";
    private static final String REPOSITORY  = "/git/repository";

    /** Loader to be displayed. */
    private final AsyncRequestLoader     loader;
    private final WsAgentStateController wsAgentStateController;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final AppContext             appContext;

    @Inject
    protected GitServiceClientImpl(LoaderFactory loaderFactory,
                                   WsAgentStateController wsAgentStateController,
                                   DtoFactory dtoFactory,
                                   AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   AppContext appContext) {
        this.appContext = appContext;
        this.loader = loaderFactory.newLoader();
        this.wsAgentStateController = wsAgentStateController;
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void init(ProjectConfigDto project, boolean bare, final RequestCallback<Void> callback)
            throws WebSocketException {

        String url = INIT + "?projectPath=" + project.getPath() + "&bare=" + bare;

        MessageBuilder builder = new MessageBuilder(POST, url);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public void cloneRepository(ProjectConfigDto project,
                                String remoteUri,
                                String remoteName,
                                RequestCallback<RepoInfo> callback) throws WebSocketException {
        CloneRequest cloneRequest = dtoFactory.createDto(CloneRequest.class)
                                              .withRemoteName(remoteName)
                                              .withRemoteUri(remoteUri)
                                              .withWorkingDir(project.getPath());

        String params = "?projectPath=" + project.getPath();

        String url = CLONE + params;

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(cloneRequest))
               .header(CONTENTTYPE, APPLICATION_JSON)
               .header(ACCEPT, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    private void sendMessageToWS(final @NotNull Message message, final @NotNull RequestCallback<?> callback) {
        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus arg) throws OperationException {
                try {
                    arg.send(message, callback);
                } catch (WebSocketException e) {
                    throw new OperationException(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void statusText(ProjectConfigDto project, StatusFormat format, AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath() + "&format=" + format;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;

        asyncRequestFactory.createGetRequest(url)
                           .loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    @Override
    public void add(ProjectConfigDto project,
                    boolean update,
                    @Nullable List<String> filePattern,
                    RequestCallback<Void> callback) throws WebSocketException {
        AddRequest addRequest = dtoFactory.createDto(AddRequest.class).withUpdate(update);
        if (filePattern == null) {
            addRequest.setFilePattern(AddRequest.DEFAULT_PATTERN);
        } else {
            addRequest.setFilePattern(filePattern);
        }
        String url = ADD + "?projectPath=" + project.getPath();

        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(addRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public void commit(ProjectConfigDto project,
                       String message,
                       boolean all,
                       boolean amend,
                       AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(all);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public void commit(final ProjectConfigDto project,
                       final String message,
                       final List<String> files,
                       final boolean amend,
                       final AsyncRequestCallback<Revision> callback) {
        CommitRequest commitRequest = dtoFactory.createDto(CommitRequest.class)
                                                .withMessage(message)
                                                .withAmend(amend)
                                                .withAll(false)
                                                .withFiles(files);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMIT + "?projectPath=" + project.getPath();

        asyncRequestFactory.createPostRequest(url, commitRequest).loader(loader).send(callback);
    }

    @Override
    public void config(ProjectConfigDto project,
                       @Nullable List<String> requestedConfig,
                       AsyncRequestCallback<Map<String, String>> callback) {
        String params = "?projectPath=" + project.getPath();
        if (requestedConfig != null) {
            for (String entry : requestedConfig) {
                params += "&requestedConfig=" + entry;
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CONFIG + params;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public void push(ProjectConfigDto project,
                     List<String> refSpec,
                     String remote,
                     boolean force,
                     AsyncRequestCallback<PushResponse> callback) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class).withRemote(remote).withRefSpec(refSpec).withForce(force);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + PUSH + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pushRequest).send(callback);
    }

    @Override
    public Promise<PushResponse> push(ProjectConfigDto project, List<String> refSpec, String remote, boolean force) {
        PushRequest pushRequest = dtoFactory.createDto(PushRequest.class)
                                            .withRemote(remote)
                                            .withRefSpec(refSpec)
                                            .withForce(force);
        return asyncRequestFactory.createPostRequest(appContext.getDevMachine().getWsAgentBaseUrl() + PUSH +
                                                     "?projectPath=" + project.getPath(), pushRequest)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(PushResponse.class));
    }

    @Override
    public void remoteList(ProjectConfigDto project,
                           @Nullable String remoteName,
                           boolean verbose,
                           AsyncRequestCallback<List<Remote>> callback) {
        String params = "?projectPath=" + project.getPath() + (remoteName != null ? "&remoteName=" + remoteName : "") +
                        "&verbose=" + String.valueOf(verbose);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + params;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public Promise<List<Remote>> remoteList(ProjectConfigDto project, @Nullable String remoteName, boolean verbose) {

        String params = "?projectPath=" + project.getPath() + (remoteName != null ? "&remoteName=" + remoteName : "") +
                        "&verbose=" + String.valueOf(verbose);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class));
    }

    @Override
    public void branchList(ProjectConfigDto project,
                           BranchListMode listMode,
                           AsyncRequestCallback<List<Branch>> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.getPath() + "&listMode=" +
                     listMode;
        asyncRequestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public Promise<Status> status(ProjectConfigDto project) {
        final String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .header(CONTENTTYPE, APPLICATION_JSON)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Status.class));
    }

    @Override
    public void status(ProjectConfigDto project, AsyncRequestCallback<Status> callback) {
        String params = "?projectPath=" + project.getPath() + "&format=" + PORCELAIN;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + STATUS + params;
        asyncRequestFactory.createGetRequest(url).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }

    @Override
    public void branchDelete(ProjectConfigDto project,
                             String name,
                             boolean force,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.getPath()
                     + "&name=" + name + "&force=" + force;
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public void branchRename(ProjectConfigDto project,
                             String oldName,
                             String newName,
                             AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath() + "&oldName=" + oldName + "&newName=" + newName;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + params;
        asyncRequestFactory.createPostRequest(url, null).loader(loader)
                           .header(CONTENTTYPE, MimeType.APPLICATION_FORM_URLENCODED)
                           .send(callback);
    }

    @Override
    public void branchCreate(ProjectConfigDto project, String name, String startPoint,
                             AsyncRequestCallback<Branch> callback) {
        BranchCreateRequest branchCreateRequest = dtoFactory.createDto(BranchCreateRequest.class).withName(name).withStartPoint(startPoint);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + BRANCH + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, branchCreateRequest).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    @Override
    public void checkout(ProjectConfigDto project,
                         CheckoutRequest checkoutRequest,
                         AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CHECKOUT + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, checkoutRequest).loader(loader).send(callback);
    }

    @Override
    public void remove(ProjectConfigDto project,
                       List<String> items,
                       boolean cached,
                       AsyncRequestCallback<String> callback) {
        String params = "?projectPath=" + project.getPath();
        if (items != null) {
            for (String item : items) {
                params += "&items=" + item;
            }
        }
        params += "&cashed" + String.valueOf(cached);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOVE + "?projectPath=" + params;
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public void reset(ProjectConfigDto project,
                      String commit,
                      @Nullable ResetRequest.ResetType resetType,
                      @Nullable List<String> filePattern,
                      AsyncRequestCallback<Void> callback) {
        ResetRequest resetRequest = dtoFactory.createDto(ResetRequest.class).withCommit(commit);
        if (resetType != null) {
            resetRequest.setType(resetType);
        }
        if (filePattern != null) {
            resetRequest.setFilePattern(filePattern);
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + RESET + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, resetRequest).loader(loader).send(callback);
    }

    @Override
    public void log(ProjectConfigDto project, List<String> fileFilter, boolean isTextFormat,
                    @NotNull AsyncRequestCallback<LogResponse> callback) {
        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.getPath());
        if (fileFilter != null) {
            for (String file : fileFilter) {
                params.append("&fileFilter=").append(file);
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + LOG + params;
        if (isTextFormat) {
            asyncRequestFactory.createGetRequest(url).send(callback);
        } else {
            asyncRequestFactory.createGetRequest(url).loader(loader).header(ACCEPT, APPLICATION_JSON).send(callback);
        }
    }

    @Override
    public void remoteAdd(ProjectConfigDto project,
                          String name,
                          String repositoryURL,
                          AsyncRequestCallback<String> callback) {
        RemoteAddRequest remoteAddRequest = dtoFactory.createDto(RemoteAddRequest.class).withName(name).withUrl(repositoryURL);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPutRequest(url, remoteAddRequest).loader(loader).send(callback);
    }

    @Override
    public void remoteDelete(ProjectConfigDto project,
                             String name,
                             AsyncRequestCallback<String> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REMOTE + '/' + name + "?projectPath=" + project.getPath();
        asyncRequestFactory.createDeleteRequest(url).loader(loader).send(callback);
    }

    @Override
    public void fetch(ProjectConfigDto project,
                      String remote,
                      List<String> refspec,
                      boolean removeDeletedRefs,
                      RequestCallback<String> callback) throws WebSocketException {
        FetchRequest fetchRequest = dtoFactory.createDto(FetchRequest.class)
                                              .withRefSpec(refspec)
                                              .withRemote(remote)
                                              .withRemoveDeletedRefs(removeDeletedRefs);

        String url = FETCH + "?projectPath=" + project.getPath();
        MessageBuilder builder = new MessageBuilder(POST, url);
        builder.data(dtoFactory.toJson(fetchRequest))
               .header(CONTENTTYPE, APPLICATION_JSON);
        Message message = builder.build();

        sendMessageToWS(message, callback);
    }

    @Override
    public void pull(ProjectConfigDto project,
                     String refSpec,
                     String remote,
                     AsyncRequestCallback<PullResponse> callback) {
        PullRequest pullRequest = dtoFactory.createDto(PullRequest.class).withRemote(remote).withRefSpec(refSpec);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + PULL + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, pullRequest).send(callback);
    }

    @Override
    public void diff(ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     String commitB, @NotNull AsyncRequestCallback<String> callback) {

        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.getPath());
        if (fileFilter != null) {
            for (String file : fileFilter) {
                params.append("&fileFilter=").append(file);
            }
        }
        params.append("&diffType=").append(type);
        params.append("&noRenames=").append(String.valueOf(noRenames));
        params.append("&renameLimit=").append(String.valueOf(renameLimit));
        params.append("&commitA=").append(commitA);
        params.append("&commitB=").append(commitB);

        String url = appContext.getDevMachine().getWsAgentBaseUrl() + DIFF + params;

        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public void showFileContent(@NotNull ProjectConfigDto project,
                                String file,
                                String version,
                                @NotNull AsyncRequestCallback<ShowFileContentResponse> callback) {
        String params = "?projectPath=" + project.getPath() + "&file=" + file + "&version=" + version ;
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + SHOW + params;
        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public void diff(ProjectConfigDto project,
                     List<String> fileFilter,
                     DiffType type,
                     boolean noRenames,
                     int renameLimit,
                     String commitA,
                     boolean cached,
                     AsyncRequestCallback<String> callback) {

        StringBuilder params = new StringBuilder().append("?projectPath=").append(project.getPath());
        if (fileFilter != null) {
            for (String file : fileFilter) {
                if (file.isEmpty()) {
                    continue;
                }
                params.append("&fileFilter=").append(file);
            }
        }
        params.append("&diffType=").append(type);
        params.append("&noRenames=").append(noRenames);
        params.append("&renameLimit=").append(renameLimit);
        params.append("&commitA=").append(commitA);
        params.append("&cached=").append(cached);

        String url = appContext.getDevMachine().getWsAgentBaseUrl() + DIFF + params;

        asyncRequestFactory.createGetRequest(url).loader(loader).send(callback);
    }

    @Override
    public void merge(ProjectConfigDto project,
                      String commit,
                      AsyncRequestCallback<MergeResult> callback) {
        MergeRequest mergeRequest = dtoFactory.createDto(MergeRequest.class).withCommit(commit);
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + MERGE + "?projectPath=" + project.getPath();
        asyncRequestFactory.createPostRequest(url, mergeRequest).loader(loader)
                           .header(ACCEPT, APPLICATION_JSON)
                           .send(callback);
    }


    @Override
    public void getCommitters(ProjectConfigDto project, AsyncRequestCallback<Commiters> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + COMMITERS + "?projectPath=" + project.getPath();
        asyncRequestFactory.createGetRequest(url).header(ACCEPT, APPLICATION_JSON).send(callback);
    }

    @Override
    public void deleteRepository(ProjectConfigDto project, AsyncRequestCallback<Void> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + REPOSITORY + "?projectPath=" + project.getPath();
        asyncRequestFactory.createRequest(DELETE, url, null, false).loader(loader)
                           .header(CONTENTTYPE, APPLICATION_JSON).header(ACCEPT, TEXT_PLAIN)
                           .send(callback);
    }

    @Override
    public void getUrlVendorInfo(@NotNull String vcsUrl, @NotNull AsyncRequestCallback<GitUrlVendorInfo> callback) {
        asyncRequestFactory.createGetRequest(appContext.getDevMachine().getWsAgentBaseUrl() + "/git-service/info?vcsurl=" + vcsUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON).send(callback);
    }
}
