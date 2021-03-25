package com.simibubi.create.content.logistics.block.depot;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DepotRenderer extends SafeTileEntityRenderer<DepotTileEntity> {

	public DepotRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(DepotTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		renderItemsOf(te, partialTicks, ms, buffer, light, overlay, te.depotBehaviour);
	}

	public static void renderItemsOf(SmartTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay, DepotBehaviour depotBehaviour) {

		TransportedItemStack transported = depotBehaviour.heldItem;
		MatrixStacker msr = MatrixStacker.of(ms);
		Vec3d itemPosition = VecHelper.getCenterOf(te.getPos());

		ms.push();
		ms.translate(.5f, 15 / 16f, .5f);

		if (transported != null)
			depotBehaviour.incoming.add(transported);

		// Render main items
		for (TransportedItemStack tis : depotBehaviour.incoming) {
			ms.push();
			msr.nudge(0);
			float offset = MathHelper.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
			float sideOffset = MathHelper.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);

			if (tis.insertedFrom.getAxis()
				.isHorizontal()) {
				Vec3d offsetVec = new Vec3d(tis.insertedFrom.getOpposite()
					.getDirectionVec()).scale(.5f - offset);
				ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);
				boolean alongX = tis.insertedFrom.rotateY()
					.getAxis() == Axis.X;
				if (!alongX)
					sideOffset *= -1;
				ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
			}

			ItemStack itemStack = tis.stack;
			int angle = tis.angle;
			Random r = new Random(0);
			renderItem(ms, buffer, light, overlay, itemStack, angle, r, itemPosition);
			ms.pop();
		}

		if (transported != null)
			depotBehaviour.incoming.remove(transported);

		// Render output items
		for (int i = 0; i < depotBehaviour.processingOutputBuffer.getSlots(); i++) {
			ItemStack stack = depotBehaviour.processingOutputBuffer.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			ms.push();
			msr.nudge(i);

			boolean renderUpright = BeltHelper.isItemUpright(stack);
			msr.rotateY(360 / 8f * i);
			ms.translate(.35f, 0, 0);
			if (renderUpright)
				msr.rotateY(-(360 / 8f * i));
			Random r = new Random(i + 1);
			int angle = (int) (360 * r.nextFloat());
			renderItem(ms, buffer, light, overlay, stack, renderUpright ? angle + 90 : angle, r, itemPosition);
			ms.pop();
		}

		ms.pop();
	}

	public static void renderItem(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay, ItemStack itemStack,
		int angle, Random r, Vec3d itemPosition) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		MatrixStacker msr = MatrixStacker.of(ms);
		int count = (int) (MathHelper.log2((int) (itemStack.getCount()))) / 2;
		boolean renderUpright = BeltHelper.isItemUpright(itemStack);
		boolean blockItem = itemRenderer.getItemModelWithOverrides(itemStack, null, null)
			.isGui3d();

		ms.push();
		msr.rotateY(angle);

		if (renderUpright) {
			Entity renderViewEntity = Minecraft.getInstance().renderViewEntity;
			if (renderViewEntity != null) {
				Vec3d positionVec = renderViewEntity.getPositionVec();
				Vec3d vectorForOffset = itemPosition;
				Vec3d diff = vectorForOffset.subtract(positionVec);
				float yRot = (float) MathHelper.atan2(diff.z, -diff.x);
				ms.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion((float) (yRot - Math.PI / 2)));
			}
			ms.translate(0, 3 / 32d, 1 / 16f);
		}

		for (int i = 0; i <= count; i++) {
			ms.push();
			if (blockItem)
				ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
			ms.scale(.5f, .5f, .5f);
			if (!blockItem && !renderUpright) {
				ms.translate(0, -3 / 16f, 0);
				msr.rotateX(90);
			}
			itemRenderer.renderItem(itemStack, TransformType.FIXED, light, overlay, ms, buffer);
			ms.pop();

			if (!renderUpright) {
				if (!blockItem)
					msr.rotateY(10);
				ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
			} else
				ms.translate(0, 0, -1 / 16f);
		}

		ms.pop();
	}

}
