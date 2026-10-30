package com.giyhub.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    public interface OnActionListener {
        void onApprove(ChatMessage message, AiAgentTools.PendingAction action);
        void onDeny(ChatMessage message);
    }

    private final List<ChatMessage> messages;
    private final OnActionListener actionListener;

    public ChatAdapter(List<ChatMessage> messages, OnActionListener actionListener) {
        this.messages = messages;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        if (msg.isUser) {
            holder.layoutUserContainer.setVisibility(View.VISIBLE);
            holder.layoutAgentContainer.setVisibility(View.GONE);
            holder.tvUserMessage.setText(msg.text);

            if (msg.attachmentUri != null) {
                holder.tvUserAttachment.setVisibility(View.VISIBLE);
                holder.tvUserAttachment.setText("📎 " + msg.attachmentUri.getLastPathSegment());
            } else {
                holder.tvUserAttachment.setVisibility(View.GONE);
            }
        } else {
            holder.layoutUserContainer.setVisibility(View.GONE);
            holder.layoutAgentContainer.setVisibility(View.VISIBLE);
            holder.tvAgentMessage.setText(msg.text);

            // Thinking Process Accordion
            if (msg.thinkingProcess != null && !msg.thinkingProcess.isEmpty()) {
                holder.cardThinking.setVisibility(View.VISIBLE);
                holder.tvThinkingBody.setText(msg.thinkingProcess);
                holder.tvThinkingBody.setVisibility(msg.isThinkingExpanded ? View.VISIBLE : View.GONE);
                holder.tvThinkingHeader.setText(msg.isThinkingExpanded ? "🧠 Thought Process (tap to collapse)" : "🧠 Thought Process (tap to expand)");

                holder.cardThinking.setOnClickListener(v -> {
                    msg.isThinkingExpanded = !msg.isThinkingExpanded;
                    notifyItemChanged(position);
                });
            } else {
                holder.cardThinking.setVisibility(View.GONE);
            }

            // Tool Result Banner
            if (msg.toolResult != null && !msg.toolResult.isEmpty()) {
                holder.tvToolResult.setVisibility(View.VISIBLE);
                holder.tvToolResult.setText("⚡ " + msg.toolResult);
            } else {
                holder.tvToolResult.setVisibility(View.GONE);
            }

            // Pending Action Confirmation (Always Ask Mode)
            if (msg.pendingAction != null) {
                holder.cardPendingAction.setVisibility(View.VISIBLE);
                holder.tvPendingActionTitle.setText("Pending: " + msg.pendingAction.toolName);
                holder.tvPendingActionDesc.setText(msg.pendingAction.description);

                holder.btnApproveAction.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onApprove(msg, msg.pendingAction);
                    }
                });

                holder.btnDenyAction.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeny(msg);
                    }
                });
            } else {
                holder.cardPendingAction.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUserContainer;
        TextView tvUserAttachment;
        TextView tvUserMessage;

        LinearLayout layoutAgentContainer;
        CardView cardThinking;
        TextView tvThinkingHeader;
        TextView tvThinkingBody;
        TextView tvAgentMessage;
        TextView tvToolResult;

        CardView cardPendingAction;
        TextView tvPendingActionTitle;
        TextView tvPendingActionDesc;
        Button btnApproveAction;
        Button btnDenyAction;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUserContainer = itemView.findViewById(R.id.layoutUserContainer);
            tvUserAttachment = itemView.findViewById(R.id.tvUserAttachment);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);

            layoutAgentContainer = itemView.findViewById(R.id.layoutAgentContainer);
            cardThinking = itemView.findViewById(R.id.cardThinking);
            tvThinkingHeader = itemView.findViewById(R.id.tvThinkingHeader);
            tvThinkingBody = itemView.findViewById(R.id.tvThinkingBody);
            tvAgentMessage = itemView.findViewById(R.id.tvAgentMessage);
            tvToolResult = itemView.findViewById(R.id.tvToolResult);

            cardPendingAction = itemView.findViewById(R.id.cardPendingAction);
            tvPendingActionTitle = itemView.findViewById(R.id.tvPendingActionTitle);
            tvPendingActionDesc = itemView.findViewById(R.id.tvPendingActionDesc);
            btnApproveAction = itemView.findViewById(R.id.btnApproveAction);
            btnDenyAction = itemView.findViewById(R.id.btnDenyAction);
        }
    }
}
