"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";
import type { ActionResult } from "@/lib/actions";
import type { OrderUpdateInput } from "@/lib/types";
import { ORDER_STATUSES, PAYMENT_STATUSES } from "@/lib/constants";
import { orderStatusLabel, paymentStatusLabel } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export function OrderStatusForm({
  orderId,
  status,
  paymentStatus,
  action,
}: {
  orderId: string;
  status: string;
  paymentStatus: string;
  action: (input: OrderUpdateInput) => Promise<ActionResult>;
}) {
  const router = useRouter();
  const [nextStatus, setNextStatus] = React.useState(status);
  const [nextPayment, setNextPayment] = React.useState(paymentStatus);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [saved, setSaved] = React.useState(false);

  const dirty = nextStatus !== status || nextPayment !== paymentStatus;

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    setSaved(false);

    const result = await action({
      id: orderId,
      status: nextStatus,
      payment_status: nextPayment,
    });

    if (result.ok) {
      setSaved(true);
      router.refresh();
    } else {
      setError(result.error);
    }
    setBusy(false);
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        <Label htmlFor="order-status">حالة الطلب</Label>
        <Select value={nextStatus} onValueChange={setNextStatus}>
          <SelectTrigger id="order-status" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {ORDER_STATUSES.map((value) => (
              <SelectItem key={value} value={value}>
                {orderStatusLabel(value)}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-2">
        <Label htmlFor="order-payment">حالة الدفع</Label>
        <Select value={nextPayment} onValueChange={setNextPayment}>
          <SelectTrigger id="order-payment" className="w-full">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {PAYMENT_STATUSES.map((value) => (
              <SelectItem key={value} value={value}>
                {paymentStatusLabel(value)}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {error ? (
        <p
          role="alert"
          className="rounded-md border border-destructive/30 bg-brand-error-container px-3 py-2 text-sm text-brand-error-on"
        >
          {error}
        </p>
      ) : null}

      {saved && !dirty ? (
        <p className="text-sm text-brand-green">تم حفظ التغييرات.</p>
      ) : null}

      <Button type="submit" disabled={busy || !dirty}>
        {busy ? <Loader2 className="size-4 animate-spin" /> : null}
        حفظ
      </Button>
    </form>
  );
}
