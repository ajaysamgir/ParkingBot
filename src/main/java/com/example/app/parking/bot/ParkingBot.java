// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.app.parking.bot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.SuggestedActions;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would
 * be added. For this sample, the {@link #onMessageActivity(TurnContext)} echos
 * the text back to the user and updates the shared
 * {@link ConversationReferences}. The
 * {@link #onMembersAdded(List, TurnContext)} will send a greeting to new
 * conversation participants with instructions for sending a proactive message.
 * </p>
 */
@Component
public class ParkingBot extends ActivityHandler {
	@Value("${server.port:8080}")
	private int port;

	private static final String WELCOME_MESSAGE = "Hello! Welcome to pay and park service";

	private static final String UTTERANCE_OPTIONS = "Please select options from below";

	@Override
	protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
		// Extract the text from the message activity the user sent.
		String text = turnContext.getActivity().getText();

		// Take the input from the user and create the appropriate response.
		String responseText = processInput(text);

		// Respond to the user.
		return turnContext.sendActivities(MessageFactory.text(responseText), createSuggestedActions())
				.thenApply(responses -> null);
	}

	protected String processInput(String message) {
		String response;
		switch (message) {
		case "Initialization":
			response = "This operation initialize parking slots";
			break;
		case "Park My Car":
			response = "This operation allocate parking slot to car if available";
			break;
		case "I want to leave":
			response = "This operation release the parking slot and generate the bill";
			break;
		default:
			response = "Echo: " + message;
		}
		return response;
	}

	@Override
	protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
		return sendWelcomeMessage(turnContext);
	}

	private CompletableFuture<Void> sendWelcomeMessage(TurnContext turnContext) {
		return turnContext.getActivity().getMembersAdded().stream()
				.filter(member -> !StringUtils.equals(member.getId(), turnContext.getActivity().getRecipient().getId()))
				.map(channel -> turnContext.sendActivities(MessageFactory.text(WELCOME_MESSAGE),
						createSuggestedActions()))
				.collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
	}

	private Activity createSuggestedActions() {
		Activity reply = MessageFactory.text(UTTERANCE_OPTIONS);

		reply.setSuggestedActions(new SuggestedActions() {
			{
				setActions(Arrays.asList(new CardAction() {
					{
						setTitle("Entry");
						setType(ActionTypes.IM_BACK);
						setValue("Park My Car");
					}
				}, new CardAction() {
					{
						setTitle("Exit");
						setType(ActionTypes.IM_BACK);
						setValue("I want to leave");
					}
				}, new CardAction() {
					{
						setTitle("Information");
						setType(ActionTypes.IM_BACK);
						setValue("Give me policy details");
					}
				}));
			}
		});

		return reply;
	}
}