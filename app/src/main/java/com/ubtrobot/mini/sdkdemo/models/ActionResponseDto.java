package com.ubtrobot.mini.sdkdemo.models;
import lombok.Data;
import java.util.List;

@Data
public class ActionResponseDto {
    public List<ActionCardDto> action_cards;
    public List<SimpleActionDto> actions;

    @Data
    public static class ActionCardDto {
        private CardDetail action;
        private CardDetail direction;
        private CardDetail step;
    }

    @Data
    public static class CardDetail {
        private String color;
        private String direction; // có thể null
        private Integer value;    // có thể null
    }

    @Data
    public static class SimpleActionDto {
        private String action;
        private Integer value;
    }
}

