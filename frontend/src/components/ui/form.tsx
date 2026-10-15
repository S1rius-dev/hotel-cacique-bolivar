"use client";

import * as React from "react";
import type { Label as LabelPrimitive } from "radix-ui";
import { Slot } from "radix-ui";
import {
  Controller,
  FormProvider,
  useFormContext,
  useFormState,
  type ControllerProps,
  type FieldPath,
  type FieldValues,
} from "react-hook-form";

import { cn } from "@/lib/utils";
import { Label } from "./label";

/* =========================
   PROVIDER
========================= */
const Form = FormProvider;

/* =========================
   CONTEXT
========================= */
type FormFieldContextValue<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
> = {
  name: TName;
};

const FormFieldContext = React.createContext<FormFieldContextValue>(
  {} as FormFieldContextValue,
);

const FormItemContext = React.createContext<{ id: string }>(
  {} as { id: string },
);

/* =========================
   FIELD WRAPPER
========================= */
const FormField = <
  TFieldValues extends FieldValues,
  TName extends FieldPath<TFieldValues>,
>(
  props: ControllerProps<TFieldValues, TName>,
) => {
  return (
    <FormFieldContext.Provider value={{ name: props.name }}>
      <Controller {...props} />
    </FormFieldContext.Provider>
  );
};

/* =========================
   FIELD STATE HOOK
========================= */
const useFormField = () => {
  const fieldContext = React.useContext(FormFieldContext);
  const itemContext = React.useContext(FormItemContext);

  const { getFieldState } = useFormContext();
  const formState = useFormState({ name: fieldContext.name });
  const fieldState = getFieldState(fieldContext.name, formState);

  const { id } = itemContext;

  return {
    id,
    name: fieldContext.name,
    formItemId: `${id}-form-item`,
    formMessageId: `${id}-form-message`,
    ...fieldState,
  };
};

/* =========================
   ITEM
========================= */
function FormItem({ className, ...props }: React.ComponentProps<"div">) {
  const id = React.useId();

  return (
    <FormItemContext.Provider value={{ id }}>
      <div className={cn("grid gap-1 relative group", className)} {...props} />
    </FormItemContext.Provider>
  );
}

/* =========================
   LABEL (ESTILO MODERNO)
========================= */
function FormLabel({
  className,
  ...props
}: React.ComponentProps<typeof LabelPrimitive.Root>) {
  const { error, formItemId } = useFormField();

  return (
    <Label
      htmlFor={formItemId}
      data-error={!!error}
      className={cn(
        "text-sm font-medium transition-colors",
        "group-focus-within:text-rose-800/90",
        "data-[error=true]:text-red-500",
        className,
      )}
      {...props}
    />
  );
}

/* =========================
   CONTROL (FOCUS GLOBAL)
========================= */
function FormControl({
  className,
  ...props
}: React.ComponentProps<typeof Slot.Root>) {
  const { error, formItemId, formMessageId } = useFormField();

  return (
    <Slot.Root
      id={formItemId}
      aria-invalid={!!error}
      aria-describedby={error ? formMessageId : undefined}
      className={cn(
        "transition-all duration-200",
        "focus-within:ring-2 focus-within:ring-rose-800/40",
        "focus-within:border-rose-800/90",
        error && "ring-2 ring-red-500/30 border-red-500",
        className,
      )}
      {...props}
    />
  );
}

/* =========================
   ERROR MESSAGE
========================= */
function FormMessage({ className, ...props }: React.ComponentProps<"p">) {
  const { error, formMessageId } = useFormField();

  if (!error) return null;

  return (
    <p
      id={formMessageId}
      className={cn("text-xs text-red-500 mt-1 animate-in fade-in", className)}
      {...props}>
      {String(error.message)}
    </p>
  );
}

export { Form, FormField, FormItem, FormLabel, FormControl, FormMessage };
