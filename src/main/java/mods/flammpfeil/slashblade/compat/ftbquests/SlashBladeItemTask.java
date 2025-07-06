package mods.flammpfeil.slashblade.compat.ftbquests;

import java.util.function.Predicate;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SlashBladeItemTask extends Task implements Predicate<ItemStack> {
	public static TaskType TYPE;
	//TODO 适配RequestDefinition或将参数全部提出来
	//TODO 实在不行的话就直接单ID和杀敌耀魂精锻进行判断得了
	private RequestDefinition request;
	private Tristate consumeItems;
	private Tristate onlyFromCrafting;
	private boolean taskScreenOnly;
	
	public SlashBladeItemTask(long id, Quest quest) {
		super(id, quest);
		consumeItems = Tristate.DEFAULT;
		onlyFromCrafting = Tristate.DEFAULT;
		taskScreenOnly = false;
	}

	@Override
	public TaskType getType() {
		return TYPE;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillConfigGroup(ConfigGroup config) {
		super.fillConfigGroup(config);
		
		config.addEnum("consume_items", consumeItems, v -> consumeItems = v, Tristate.NAME_MAP);
		config.addEnum("only_from_crafting", onlyFromCrafting, v -> onlyFromCrafting = v, Tristate.NAME_MAP);
		config.addBool("task_screen_only", taskScreenOnly, v -> taskScreenOnly = v, false);
	}

	@Override
	public boolean test(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return true;
		}
		
		return this.request.test(itemStack);
	}
}
