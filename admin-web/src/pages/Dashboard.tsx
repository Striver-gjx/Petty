import { useEffect, useState } from 'react';
import { TrendingUp, Users, UserCheck, ClipboardList, PawPrint } from 'lucide-react';
import { ownerApi, sitterApi, orderApi, serviceTypeApi } from '../api';

interface Stats {
  owners: number;
  sitters: number;
  orders: number;
  serviceTypes: number;
}

export default function Dashboard() {
  const [stats, setStats] = useState<Stats>({ owners: 0, sitters: 0, orders: 0, serviceTypes: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      ownerApi.list(),
      sitterApi.list(),
      orderApi.listAll(),
      serviceTypeApi.list(),
    ]).then(([owners, sitters, orders, types]) => {
      setStats({
        owners: (owners as { data: unknown[] }).data.length,
        sitters: (sitters as { data: unknown[] }).data.length,
        orders: (orders as { data: unknown[] }).data.length,
        serviceTypes: (types as { data: unknown[] }).data.length,
      });
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const cards = [
    { label: '宠物主人', value: stats.owners, icon: Users, color: 'bg-blue-500' },
    { label: '喂养师', value: stats.sitters, icon: UserCheck, color: 'bg-green-500' },
    { label: '服务订单', value: stats.orders, icon: ClipboardList, color: 'bg-purple-500' },
    { label: '服务类型', value: stats.serviceTypes, icon: PawPrint, color: 'bg-orange-500' },
  ];

  if (loading) {
    return <div className="text-gray-400">加载中...</div>;
  }

  return (
    <div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {cards.map((card) => (
          <div key={card.label} className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">{card.label}</p>
                <p className="text-3xl font-bold text-gray-800 mt-1">{card.value}</p>
              </div>
              <div className={`${card.color} p-3 rounded-lg`}>
                <card.icon className="w-6 h-6 text-white" />
              </div>
            </div>
          </div>
        ))}
      </div>
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <div className="flex items-center gap-2 mb-4">
          <TrendingUp className="w-5 h-5 text-gray-400" />
          <h2 className="text-lg font-semibold text-gray-800">快速开始</h2>
        </div>
        <p className="text-gray-500">后端 API 已启动，可通过左侧导航管理喂养师、宠物主人和服务订单。</p>
      </div>
    </div>
  );
}
