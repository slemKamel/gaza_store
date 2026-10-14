import { Badge } from "@/components/ui/badge";
import { orderStatusLabel, paymentStatusLabel } from "@/lib/constants";

type Variant = "default" | "success" | "warning" | "danger" | "outline";

const STATUS_VARIANTS: Record<string, Variant> = {
  pending: "warning",
  confirmed: "default",
  shipped: "default",
  delivered: "success",
  cancelled: "danger",
};

export function OrderStatusBadge({ status }: { status: string }) {
  return (
    <Badge variant={STATUS_VARIANTS[status] ?? "outline"}>
      {orderStatusLabel(status)}
    </Badge>
  );
}

const PAYMENT_VARIANTS: Record<string, Variant> = {
  unpaid: "warning",
  paid: "success",
  refunded: "danger",
};

export function PaymentStatusBadge({ status }: { status: string }) {
  return (
    <Badge variant={PAYMENT_VARIANTS[status] ?? "outline"}>
      {paymentStatusLabel(status)}
    </Badge>
  );
}
