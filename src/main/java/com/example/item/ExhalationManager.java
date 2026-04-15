package com.example.item;

import com.example.ExampleMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExhalationManager {

    private static class ExhalationTask {
        final ServerPlayerEntity player;
        int ticksRemaining;

        ExhalationTask(ServerPlayerEntity player, int durationTicks) {
            this.player = player;
            this.ticksRemaining = durationTicks;
        }
    }

    private static final List<ExhalationTask> ACTIVE_TASKS = new ArrayList<>();

    /**
     * Запускает процесс выдоха для игрока на 3 секунды (60 тиков).
     * Дым будет лететь туда, куда игрок смотрит в каждый момент времени.
     */
    public static void startExhalation(ServerPlayerEntity player) {
        // 60 тиков = 3 секунды
        ACTIVE_TASKS.add(new ExhalationTask(player, 60));
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ExhalationTask> iterator = ACTIVE_TASKS.iterator();
            while (iterator.hasNext()) {
                ExhalationTask task = iterator.next();

                // Если игрок вышел с сервера или умер — прекращаем выдох
                if (task.player.isRemoved() || !task.player.isAlive()) {
                    iterator.remove();
                    continue;
                }

                if (task.player.getWorld() instanceof ServerWorld serverWorld) {
                    // Каждый тик берём АКТУАЛЬНЫЙ вектор взгляда игрока
                    Vec3d look = task.player.getRotationVec(1.0F).normalize();

                    // Разлёт скорости — делаем поток шире и рассеяннее
                    double velSpread = 0.045; 
                    
                    // Разлёт позиции — чтобы дым спавнился не из одной точки, а немного объёмно
                    double posSpread = 0.15;
                    double offsetX = (serverWorld.random.nextDouble() - 0.5) * posSpread;
                    double offsetY = (serverWorld.random.nextDouble() - 0.5) * posSpread;
                    double offsetZ = (serverWorld.random.nextDouble() - 0.5) * posSpread;

                    // Точка спавна: чуть впереди лица игрока + случайное смещение
                    double baseX = task.player.getX() + look.x * 0.5 + offsetX;
                    double baseY = task.player.getEyeY() - 0.1 + offsetY;
                    double baseZ = task.player.getZ() + look.z * 0.5 + offsetZ;

                    // Скорость частицы — по направлению взгляда, но с рассеянием
                    double forwardSpeed = 0.04;
                    double vX = look.x * forwardSpeed + (serverWorld.random.nextDouble() - 0.5) * velSpread;
                    double vY = look.y * forwardSpeed + (serverWorld.random.nextDouble() - 0.5) * velSpread;
                    double vZ = look.z * forwardSpeed + (serverWorld.random.nextDouble() - 0.5) * velSpread;

                    // count=0 → 1 частица с точным вектором скорости
                    serverWorld.spawnParticles(ExampleMod.CIGARETTE_CLOUD, baseX, baseY, baseZ, 0, vX, vY, vZ, 1.0);
                }

                task.ticksRemaining--;
                if (task.ticksRemaining <= 0) {
                    iterator.remove();
                }
            }
        });
    }
}
