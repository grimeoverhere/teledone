package com.goh.teledone.telegrambot.ability;

import com.goh.teledone.telegrambot.TeledoneAbilityBot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.util.AbilityExtension;

import javax.annotation.PostConstruct;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
@RequiredArgsConstructor
public class DescribeSMARTTastAbility implements AbilityExtension {

    private static final String WHAT_SMART_TASK_IS = """
            *What is SMART in Project Management?*
            SMART refers to a specific criteria for setting goals and project objectives. SMART stands for Specific, Measurable, Attainable, Relevant, and Time-bound. The idea is that every project goal must adhere to the SMART criteria to be effective. Therefore, when planning a project's objectives, each one should be:
                        
            *Specific*: The goal should target a specific area of improvement or answer a specific need
            *Measurable*: The goal must be quantifiable, or at least allow for measurable progress
            *Attainable*: The goal should be realistic, based on available resources and existing constraints
            *Relevant*: The goal should align with other business objectives to be considered worthwhile
            *Time-bound*: The goal must have a deadline or defined end
            
            Let’s look at each step of the SMART criteria in detail.
                        
            *What are SMART goals?*
            *Specific*
            The goal should target a specific area of improvement or answer a specific need. Because it’s the first step in the SMART goal process, it’s important to be as clear as possible. For example, note the difference between “I will make lunch” and “I will use wheat toast, peanut butter, and strawberry jam to create a tasty sandwich for myself to eat”. See how specific it is? This example also illustrates the importance of word choice. Not only are you noting which ingredients or tools will be used to achieve the final goal, but you’re also articulating who benefits. Details like these color your goal description, making it easier for collaborators to visualize and align intentions with your project.
                        
            *Measurable*
            The goal must be quantifiable, or at least allow for measurable progress. In this step, you’ll choose what your progress markers or project KPIs are and how you’ll measure them. This might mean adopting the right tools or restructuring your KPI’s to something that you can easily monitor. You’ll also need to define who is in charge of measuring your progress, when these measurements will take place, and where the information will be shared.
                        
            *Attainable*
            The goal should be realistic and based on available resources and existing constraints. Typical project constraints include team bandwidth, budgets, and timelines. Project managers should look to data from similar past projects for insight into what’s achievable this time around.
                        
            *Relevant*
            The goal should align with other business objectives to be considered worthwhile. You can also break your project goal down into smaller, equally relevant goals that will keep the whole team focused. Be diligent about eliminating irrelevant goals and subgoals to save significant time.
                        
            *Time-bound*
            The goal must have a deadline or a defined end. This can be measured in hours and minutes, business days, or years depending on the project scope. To set your project timelines, get feedback from major stakeholders about their deadline expectations, and compare it to team members' inputs.
            """;

    @NonNull
    private TeledoneAbilityBot abilityBot;

    @PostConstruct
    public void activateAbility() {
        abilityBot.addExtension(this);
    }

    public Ability getInbox() {
        return Ability.builder()
                .name("smart")
                .privacy(PUBLIC)
                .locality(ALL)
                .setStatsEnabled(true)
                .info("Returns description of SMART task.")
                .action(context -> context.bot().silent().sendMd(WHAT_SMART_TASK_IS, context.chatId()))
                .build();
    }



}
