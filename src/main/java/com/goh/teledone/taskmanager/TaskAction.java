package com.goh.teledone.taskmanager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TaskAction {
    MOVE_TODAY("TODAY"),
    MOVE_TO_THIS_WEEK("WEEK"),
    MOVE_TO_BACKLOG("BACKLOG"),
    MARK_DONE("\uD83D\uDD30"), //https://emojipedia.org/japanese-symbol-for-beginner/
    EDIT("\uD83D\uDCDD"), // https://emojipedia.org/memo/
    DELETE("\uD83D\uDDD1️"); // https://emojipedia.org/wastebasket/
//    CANCEL //todo:: нужен ли такой action?

    private String actionTextForButton;

}
