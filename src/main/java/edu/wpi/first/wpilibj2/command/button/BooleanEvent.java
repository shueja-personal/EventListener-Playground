// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj2.command.button;

import static edu.wpi.first.wpilibj.util.ErrorMessages.requireNonNullParam;

import edu.wpi.first.math.filter.Debouncer;

import java.util.Collection;
import java.util.LinkedHashSet;
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
  protected final Collection<Runnable> m_handlers;

  /**
   * Creates a new BooleanEvent that monitors the given condition.
   *
   * @param eventSupplier the condition the BooleanEvent should monitor.
   */
  public BooleanEvent(BooleanSupplier eventSupplier) {
    this(eventSupplier, new LinkedHashSet<>());
  }

  protected BooleanEvent(BooleanSupplier eventSupplier, Collection<Runnable> handlers) {
    m_eventSupplier = eventSupplier;
    m_handlers = handlers;
  }

  /**
   * Creates a new BooleanEvent that is always false. Useful only as a no-arg constructor for
   * subclasses that will be overriding {@link BooleanEvent#get()} anyway.
   */
  public BooleanEvent() {
    this(()->false);
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

  public void poll() {
    for (Runnable handler : m_handlers) {
      handler.run();
    }
  }

  protected void addHandler(Runnable handler) {
    m_handlers.add(handler);
  }

  /**
   * Removes all bindings from this BooleanEvent.
   */
  public void clearBindings() {
    m_handlers.clear();
  }

  /* RUNNABLES */

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
   * @param toRun the Runnable to run while the BooleanEvent is true.
   * @return this BooleanEvent, so calls can be chained
   */
  public BooleanEvent whileTrueContinuous(final Runnable toRun) {
    addHandler(
      () -> {
        if(get() == true) {
          toRun.run();
        }
      }
    );
    return this;
  }

  /**
   * Runs the given Consumer when this BooleanEvent changes state between true and false.
   * 
   * @param handler the Consumer to run, which accepts the new state
   * @return this BooleanEvent, so calls can be chained
   */
  protected BooleanEvent onChange(Consumer<Boolean> handler) {
    requireNonNullParam(handler, "handle", "onChange");
    addHandler(
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
    Collection<Runnable> handlers = m_handlers;
    handlers.addAll(eventListener.m_handlers);
    return new BooleanEvent(
      () -> get() && eventListener.get(),
      handlers);
  }
  /**
   * Composes this BooleanEvent with another BooleanEvent, returning a new BooleanEvent that is true when either
   * BooleanEvent is true.
   *
   * @param eventListener the BooleanEvent to compose with
   * @return the BooleanEvent that is true when either BooleanEvent is true
   */
  public BooleanEvent or(BooleanEvent eventListener) {
    Collection<Runnable> handlers = m_handlers;
    handlers.addAll(eventListener.m_handlers);
    return new BooleanEvent(
      () -> get() || eventListener.get(),
      handlers);
  }

  /**
   * Creates a new BooleanEvent that is true when this BooleanEvent is false, i.e. that acts as the
   * negation of this BooleanEvent.
   * 
   * <p>IMPORTANT: the bindings from the BooleanEvent being negated will not be copied,
   * to avoid actions being bound to the negation of their intended condition
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
        },
        m_handlers);
  }
}
