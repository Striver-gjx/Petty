import { useEffect, useState } from 'react';
import { ClipboardList } from 'lucide-react';
import { orderApi } from '../api';

interface Order {
  id: number;
  orderNo: string;
  ownerId: number;
  sitterId: number | null;
  serviceAddress: string;
  scheduledDate: string;
  totalAmount: number;
  status: string;
  paymentStatus: string;
  createdAt: string;
}

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING_MATCH: { label: '待匹配', color: 'bg-yellow-100 text-yellow-700' },
  PENDING_ACCEPT: { label: '待接单', color: 'bg-blue-100 text-blue-700' },
  ACCEPTED: { label: '已接单', color: 'bg-indigo-100 text-indigo-700' },
  SITTER_EN_ROUTE: { label: '前往中', color: 'bg-cyan-100 text-cyan-700' },
  IN_SERVICE: { label: '服务中', color: 'bg-green-100 text-green-700' },
  SERVICE_COMPLETED: { label: '已完成', color: 'bg-emerald-100 text-emerald-700' },
  OWNER_CONFIRMED: { label: '已确认', color: 'bg-teal-100 text-teal-700' },
  CANCELLED: { label: '已取消', color: 'bg-red-100 text-red-700' },
  DISPUTED: { label: '争议中', color: 'bg-orange-100 text-orange-700' },
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    orderApi.list()
      .then((res) => setOrders((res as { data: Order[] }).data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="text-gray-400">加载中...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <ClipboardList className="w-6 h-6" /> 服务订单
        </h2>
        <span className="text-sm text-gray-400">{orders.length} 条订单</span>
      </div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left font-medium text-gray-500">订单号</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">服务地址</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">预约日期</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">金额</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">状态</th>
              <th className="px-6 py-3 text-left font-medium text-gray-500">支付</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {orders.map((o) => {
              const st = statusMap[o.status] || { label: o.status, color: 'bg-gray-100 text-gray-700' };
              return (
                <tr key={o.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-mono text-gray-800">{o.orderNo}</td>
                  <td className="px-6 py-4 text-gray-600 max-w-xs truncate">{o.serviceAddress}</td>
                  <td className="px-6 py-4 text-gray-600">{o.scheduledDate}</td>
                  <td className="px-6 py-4 font-medium text-gray-800">¥{o.totalAmount}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${st.color}`}>
                      {st.label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600">{o.paymentStatus}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {orders.length === 0 && (
          <div className="text-center py-12 text-gray-400">暂无订单数据</div>
        )}
      </div>
    </div>
  );
}
