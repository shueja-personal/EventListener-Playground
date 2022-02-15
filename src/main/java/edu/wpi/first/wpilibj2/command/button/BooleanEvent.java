// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command.button;

import static edu.wpi.first.wpilibj.util.ErrorMessages.requireNonNullParam;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * This class provides an easy way to link commands to boolean inputs such as joystick buttons, limit switches, or robot status.
 *
 * <p>It is very easy to link an event to a command. For instance, you could link the BooleanEvent button
 * of a joystick to a "score" command.
 *
 * <p>It is encouraged that teams write a subclass of BooleanEvent if they want to have something unusual
 * (for instance, if they want to react to the user holding a button while the robot is reading a
 * certain sensor input). For this, they only have to write the {@link BooleanEvent#get()} method to get
 * the full functionality of the BooleanEvent class.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public class BooleanEvent implements BooleanSupplier {
  private final BooleanSupplier m_eventSupplier;

  /**
   * Creates a new BooleanEvent that monitors the given condition.
   *
   * @param eventSupplier the condition the BooleanEvent should monitor.
   */
  public BooleanEvent(BooleanSupplier eventSupplier) {
    m_eventSupplier = eventSupplier;
  }

  /**
   * Creates a new BooleanEvent that is always false. Useful only as a no-arg constructor for
   * subclasses that will be overriding {@link BooleanEvent#get()} anyway.
   */
  public BooleanEvent() {
    m_eventSupplier = () -> false;
  }

  /**
   * Returns whether or not the BooleanEvent is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the BooleanEvent.
   *
   * <p>Functionally identical to {@link BooleanEvent#getAsBoolean()}.
   *
   * @return whether or not the BooleanEvent is true.
   */
  public boolean get() {
    return this.getAsBoolean();
  }

/**
   * Returns whether or not the BooleanEvent is true.
   *
   * <p>This method will be called repeatedly when a command is linked to the BooleanEvent.
   *
   * <p>Functionally identical to {@link BooleanEvent#get()}.
   *
   * @return whether or not the BooleanEvent condition is true.
   */
  @Override
  public boolean getAsBoolean() {
    return m_eventSupplier.getAsBoolean();
  }

  /* RUNNABLES
    Both for non-command usage and as helper methods for the Command binding methods.
  */


  /**
   * Runs the given Runnable whenever the BooleanEvent just becomes true.
   * 
   * <p>This method does not schedule any commands nor deal with requirements. To use a Command, use 
   * `onTrue(new InstantCommand(Runnable toRun, Subsystem...requirements));`
   *
   * @param toRun the Runnable to run
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Runnable toRun) {
    onChange(state -> {
      if (state == true) {
        toRun.run();
      }
    });
    return this;
  }

  /**
   * Constantly runs the given Runnable while the BooleanEvent is true.
   * 
   * <p>This method does not schedule any commands or deal with requirements. 
   * @param during the Runnable to run while the BooleanEvent is true.
   * @param after the Runnable to run when the BooleanEvent next becomes false.
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrue(final Runnable during, final Runnable after) {
    requireNonNullParam(during, "during", "whileTrue");
    requireNonNullParam(after, "after", "whileTrue");
    CommandScheduler.getInstance().addButton(
      new Runnable() {
        private boolean m_stateLast = get();

        @Override
        public void run() {
          boolean state = get();

          if (state == true) {
            during.run();
          }
          if (m_stateLast == true && state == false) {
            after.run();
          }

          m_stateLast = state;
        }
      });
    return this;
  }

    /**
   * Constantly runs the given Runnable while the BooleanEvent is true.
   * 
   * <p>This method does not schedule any commands or deal with requirements. 
   * @param toRun the Runnable to run while the BooleanEvent is true.
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrue(final Runnable toRun) {
    whileTrue(toRun, ()->{});
    return this;
  }

  /**
   * Runs the given Consumer when this BooleanEvent changes state between true and false.
   * 
   * @param handler the Consumer to run, which accepts the new state
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onChange(Consumer<Boolean> handler) {
    requireNonNullParam(handler, "handle", "onChange");
    CommandScheduler.getInstance()
        .addButton(
            new Runnable() {
              private boolean m_stateLast = get();

              @Override
              public void run() {
                boolean state = get();

                if (m_stateLast != state) {
                  handler.accept(state);
                }

                m_stateLast = state;
              }
            });
    return this;
  }

  /* COMMANDS */

  /**
   * Starts the given command whenever the BooleanEvent becomes true. 
   * 
   * <p>The command is set to be interruptible, and will not be restarted if it ends.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent onTrue(final Command command) {
    requireNonNullParam(command, "command", "onTrue");
    onTrue(()->{
      if (!command.isScheduled()) {
        command.schedule();
      }
    });
    return this;
  }

  /**
   * Runs the given command only while the BooleanEvent is true, restarting it if it ends.
   *
   * <p>{@link Command#schedule(boolean)} will be called to restart the command.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueContinuous(final Command command) {
    requireNonNullParam(command, "command", "whileTrue");
    whileTrue(
      ()->{
        if (!command.isScheduled()) {
          command.schedule();
        }
      },
      ()->{
        if (command.isScheduled()) {
          command.cancel();
        }
      });
    return this;
  }

    /**
   * Starts the given command when the BooleanEvent becomes true, and cancels it when the BooleanEvent becomes false. 
   * 
   * <p>The command will not be restarted if it ends.
   *
   * @param command the command to start
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueOnce(final Command command) {
    requireNonNullParam(command, "command", "whileTrue");
    onChange(
      state -> {
        if(state == true){
          if (!command.isScheduled()) {
            command.schedule();
          }
        }
        else {
          if (command.isScheduled()) {
            command.cancel();
          }
        }
      });
    return this;
  }
  
  /**
   * Toggles a command when the BooleanEvent becomes true.
   * 
   * <p>This method will cancel the command if it is running, or schedule it if it is not running.
   *
   * @param command the command to toggle
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent toggleOnTrue(final Command command) {
    requireNonNullParam(command, "command", "toggleOnTrue");
    onTrue(
      ()->{
        if (command.isScheduled()) {
          command.cancel();
        } else {
          command.schedule();
        }
      }
    );
    return this;
  }

  /**
   * Cancels a command when the BooleanEvent becomes true.
   *
   * @param command the command to cancel
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent cancelOnTrue(final Command command) {
    requireNonNullParam(command, "command", "cancelOnTrue");
    onTrue(
      ()-> {
        if (command.isScheduled()) {
          command.cancel();
        }
      }
    );
    return this;
  }

  /* COMPOSITION */
  
  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when both
   * BooleanEvents are true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when both BooleanEvents are true
   */
  public BooleanEvent and(BooleanEvent eventListener) {
    return new BooleanEvent(() -> get() && eventListener.get());
  }

  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when either
   * BooleanEvent is true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when either BooleanEvent is true
   */
  public BooleanEvent or(BooleanEvent eventListener) {
    return new BooleanEvent(() -> get() || eventListener.get());
  }

  /**
   * Creates a new BooleanEvent that is true when this BooleanEvent is false, i.e. that acts as the
   * negation of this BooleanEvent.
   *
   * @return the negated BooleanEvent
   */
  public BooleanEvent negate() {
    return new BooleanEvent(() -> !get());
  }

  /**
   * Creates a new debounced BooleanEvent from this BooleanEvent - it will become true when this BooleanEvent has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @return The debounced BooleanEvent (rising edges debounced only)
   */
  public BooleanEvent debounce(double seconds) {
    return debounce(seconds, Debouncer.DebounceType.kRising);
  }

  /**
   * Creates a new debounced BooleanEvent from this BooleanEvent - it will become true when this BooleanEvent has
   * been true for longer than the specified period.
   *
   * @param seconds The debounce period.
   * @param type The debounce type.
   * @return The debounced BooleanEvent.
   */
  public BooleanEvent debounce(double seconds, Debouncer.DebounceType type) {
    return new BooleanEvent(
        new BooleanSupplier() {
          Debouncer m_debouncer = new Debouncer(seconds, type);

          @Override
          public boolean getAsBoolean() {
            return m_debouncer.calculate(get());
          }
        });
  }
}
