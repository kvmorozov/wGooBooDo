var packageJSON = require('./package.json');
var path = require('path');
var webpack = require('webpack');
var nib = require('nib');

module.exports = {
    devtool: 'source-map',
    entry: './index.js',
    output: {
        path: path.join(__dirname, 'generated'),
        filename: 'app-bundle.js'
    },
    resolve: {
        extensions: ['.js', '.jsx', '.styl']
    },
    plugins: [
        new webpack.LoaderOptionsPlugin({
            debug: true,
            // test: /\.xxx$/, // may apply this only for some modules
            options: {
                stylus: {
                    // nib - CSS3 extensions for Stylus
                    use: [nib()],
                    // no need to have a '@import "nib"' in the stylesheet
                    import: ['~nib/lib/nib/index.styl']
                }
            }
        }),
        new webpack.DefinePlugin({
            "process.env": {
                NODE_ENV: JSON.stringify("development")
            }
        })
    ],
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.jsx?$/,
                loader: 'babel-loader',
                exclude: /node_modules/
            },
            {
                test: /\.styl$/,
                loader: ['style-loader', 'css-loader', 'stylus-loader']
            },
            {
                test: /\.(png|jpg)$/,
                loader: 'url-loader',
                query: {
                    limit: 8192
                }
            },
            {
                test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'url-loader',
                query: {
                    limit: 10000,
                    mimetype: 'application/font-woff'
                }
            },
            {
                test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'file-loader'
            }
        ]
    },
    devServer: {
        noInfo: false,
        quiet: false,
        lazy: false,
        watchOptions: {
            poll: true
        }
    }
}