(function () {

	requirejs.config({
		packages: [
			{name: 'rest', location: 'node_modules/rest', main: 'browser'},
			{name: 'when', location: 'node_modules/when', main: 'when'},
			{name: 'path', location: 'node_modules/path', main: 'path'},
			{name: 'webpack', location: 'node_modules/webpack/bin', main: 'webpack'},

			{name: 'react', location: 'libs/react', main: 'react'},
			{name: 'react-dom', location: 'libs/react', main: 'react-dom'},
			{name: 'jsx', location: 'libs/requirejs-react-jsx', main: 'jsx'},
			{name: 'text', location: 'libs/requirejs-text', main: 'text'},
			{name: 'client', location: 'libs', main: 'client'},
			{name: 'react-infinite-tree', location: 'libs/react-infinite-tree/dist', main: 'react-infinite-tree.min'}
		],
		shim: {
			"react": {
				"exports": "React"
			}
		},
		deps: ['main']
	});
}());