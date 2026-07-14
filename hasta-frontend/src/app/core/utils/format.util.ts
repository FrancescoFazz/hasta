const currencyFormatter = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' });

export function formatCurrency(value: number): string {
  return currencyFormatter.format(value);
}
