// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.bot.sample.proactive;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  For this sample, the {@link #onMessageActivity(TurnContext)} echos the text
 * back to the user and updates the shared {@link ConversationReferences}.
 * The {@link #onMembersAdded(List, TurnContext)} will send a greeting
 * to new conversation participants with instructions for sending a proactive message.</p>
 */
@Component
public class ParkingBot extends ActivityHandler {
    @Value("${server.port:8080}")
    private int port;

    private static final String WELCOME_MESSAGE = "Hello! Welcome to pay and park service";
    
    private static final String SELF_INTRO_MESSAGE = "My name is Jack. I am hear to help you for park your car in parking slot";
    
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());

        return turnContext
            .sendActivity(MessageFactory.text("Echo: " + turnContext.getActivity().getText()))
            .thenApply(sendResult -> null);
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
        return membersAdded.stream()
            .filter(member -> !StringUtils.equals(member.getId(), turnContext.getActivity().getRecipient().getId()))
            .map(channel -> turnContext.sendActivity(MessageFactory.text(String.format(WELCOME_MESSAGE, port))))
            .map(channel -> turnContext.sendActivity(MessageFactory.text(String.format(SELF_INTRO_MESSAGE, port))))
            .collect(CompletableFutures.toFutureList())
            .thenApply(resourceResponses -> null);
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());
        return super.onConversationUpdateActivity(turnContext);
    }

    // adds a ConversationReference to the shared Map.
    private void addConversationReference(Activity activity) {
        //ConversationReference conversationReference = activity.getConversationReference();
        // conversationReferences.put(conversationReference.getUser().getId(), conversationReference);
    }
}